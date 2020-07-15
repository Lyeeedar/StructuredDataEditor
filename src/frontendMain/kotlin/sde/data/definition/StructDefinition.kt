package sde.data.definition

import org.w3c.dom.Node
import sde.utils.DefinitionLoadException
import sde.utils.getAttributeValue

open class StructDefinition : AbstractDataDefinition()
{
	val contents = ArrayList<AbstractDataDefinition>()

	var description = ""
	var nullable = true
	var extendsAfter = ""

	init
	{
		textColour = colours["Struct"]!!
	}

	override fun children(): List<AbstractDataDefinition>
	{
		return contents
	}

	override fun doParse(node: Node)
	{
		description = node.getAttributeValue("Description", description)
		nullable = node.getAttributeValue("Nullable", nullable)
		extendsAfter = node.getAttributeValue("ExtendsAfter", extendsAfter)
		val extends = node.getAttributeValue("Extends", "")
		if (!extends.isBlank())
		{
			registerReference("Extends", extends)
		}
	}

	override fun postResolve()
	{
		val extends = referenceMap["Extends"] ?: return
		val def = extends.definition ?: return

		if (def !is StructDefinition)
		{
			throw DefinitionLoadException("Struct $name tried to extend non-struct definition ${extends.defName}")
		}

		val newChildren = ArrayList(def.contents)

		var insertIndex = if (extendsAfter.isNotBlank()) newChildren.indexOfFirst { it.name == extendsAfter } else -1
		if (insertIndex == -1) insertIndex = newChildren.size

		for (child in contents)
		{
			newChildren.add(insertIndex++, child)
		}
		attributes.addAll(def.attributes)

		contents.clear()
		contents.addAll(newChildren)
	}
}