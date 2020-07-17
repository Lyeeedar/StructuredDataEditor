package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.item.AbstractCompoundDataItem
import sde.data.item.AbstractDataItem
import sde.util.XComment
import sde.util.XElement
import sde.utils.DefinitionLoadException

typealias CategorisedChildren = Pair<String, ArrayList<DataDefinition>>

abstract class AbstractCompoundDefinition<D: AbstractCompoundDefinition<D, I>, I: AbstractCompoundDataItem<D>> : AbstractDataDefinition<D, I>()
{
	val contents = ArrayList<CategorisedChildren>()

	override fun children(): List<DataDefinition>
	{
		return sequence {
			for (category in contents)
			{
				for (child in category.second)
				{
					yield(child)
				}
			}
		}.toList()
	}

	override fun doParse(node: XElement)
	{
		val extends = node.getAttributeValue("Extends", "")
		if (extends.isNotBlank())
		{
			registerReference("Extends", extends)
		}

		val keys = node.getAttributeValue("Keys", "")
		if (keys.isNotBlank())
		{
			if (keys.contains('('))
			{
				val categories = keys.split(')')
				for (category in categories)
				{
					val split = category.split('(')
					var name = split[0].trim()
					if (name.startsWith(',')) name = name.substring(1)
					val defs = split[1].split(',').map { it.trim() }

					registerReference("Keys", defs, name)
				}
			}
			else
			{
				val defs = keys.split(',').map { it.trim() }
				registerReference("Keys", defs)
			}
		}

		var currentCategory = CategorisedChildren("", ArrayList())
		contents.add(currentCategory)

		for (child in node.children)
		{
			if (child is XComment)
			{
				currentCategory = CategorisedChildren(node.value, ArrayList())
				contents.add(currentCategory)
			}
			else if (child is XElement)
			{
				if (child.name == "Attributes" || child.name == "AdditionalDefs") continue

				val def = load(child, srcFile)
				currentCategory.second.add(def)
			}
		}

		doParseInstance(node)
	}

	abstract fun doParseInstance(node: XElement)

	override fun postResolve()
	{
		resolveExtends()
		resolveKeys()
	}

	fun resolveKeys()
	{
		val keys = getReferences("Keys")
		if (keys.isEmpty()) return

		for (key in keys)
		{
			val def = key.definition ?: continue

			addDef(def, key.category)
		}
	}

	fun resolveExtends()
	{
		val extends = getReference("Extends") ?: return
		val def = extends.definition ?: return

		if (def !is AbstractCompoundDefinition)
		{
			throw DefinitionLoadException("Def $name tried to extend non-compound definition ${extends.defName}")
		}

		for (category in def.contents)
		{
			for (def in category.second)
			{
				addDef(def, category.first)
			}
		}

		attributes.addAll(def.attributes)
	}

	fun addDef(def: DataDefinition, category: String)
	{
		var dest = contents.firstOrNull { it.first == category }
		if (dest == null)
		{
			dest = CategorisedChildren(category, ArrayList())
			contents.add(dest)
		}

		dest.second.add(def)
	}
}