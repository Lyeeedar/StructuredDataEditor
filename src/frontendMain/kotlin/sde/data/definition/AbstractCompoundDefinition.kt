package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.item.AbstractDataItem
import sde.utils.DefinitionLoadException
import sde.utils.asSequence
import sde.utils.getAttributeValue
import sde.utils.serializeXml

typealias CategorisedChildren = Pair<String, ArrayList<AbstractDataDefinition<*, *>>>

abstract class AbstractCompoundDefinition<D: AbstractCompoundDefinition<D, I>, I: AbstractDataItem<D>> : AbstractDataDefinition<D, I>()
{
	val contents = ArrayList<CategorisedChildren>()

	override fun children(): List<AbstractDataDefinition<*, *>>
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

	override fun doParse(node: Element)
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

		for (child in node.childNodes.asSequence())
		{
			if (child.nodeType == Node.COMMENT_NODE)
			{
				currentCategory = CategorisedChildren(node.textContent!!, ArrayList())
				contents.add(currentCategory)
			}
			else if (child.nodeType == Node.ELEMENT_NODE)
			{
				if (child.nodeName == "Attributes" || child.nodeName == "AdditionalDefs") continue

				val def = load(child as Element)
				currentCategory.second.add(def)
			}
		}

		doParseInstance(node)
	}

	abstract fun doParseInstance(node: Element)

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

	fun addDef(def: AbstractDataDefinition<*, *>, category: String)
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