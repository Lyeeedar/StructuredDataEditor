package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.DataDocument
import sde.data.item.AbstractCompoundDataItem
import sde.data.item.AbstractDataItem
import sde.data.item.StructItem
import sde.utils.DefinitionLoadException
import sde.utils.getAttributeValue

abstract class AbstractStructDefinition<D: AbstractStructDefinition<D, I>, I: AbstractCompoundDataItem<D>> : AbstractCompoundDefinition<D, I>()
{
	var description = ""
	var nullable = true

	init
	{
		textColour = colours["Struct"]!!
	}

	override fun doParseInstance(node: Element)
	{
		description = node.getAttributeValue("Description", description)
		nullable = node.getAttributeValue("Nullable", nullable)
	}

	override fun createItemInstance(document: DataDocument): I
	{
		val item = createItemInstanceInternal(document)

		if (!nullable)
		{
			createContents(item, document)
		}

		return item
	}

	fun createContents(item: I, document: DataDocument)
	{
		val existingDefs = item.children.map { it.def }.toSet()

		for (category in contents)
		{
			for (def in category.second)
			{
				if (!existingDefs.contains(def))
				{
					val citem = def.createItem(document)
					item.children.add(citem)
				}
			}
		}
	}

	abstract fun createItemInstanceInternal(document: DataDocument): I
}

class StructDefinition : AbstractStructDefinition<StructDefinition, StructItem>()
{
	override fun createItemInstanceInternal(document: DataDocument): StructItem
	{
		return StructItem(this, document)
	}

}