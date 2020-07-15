package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
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

	override fun createItemInstance(): I
	{
		val item = createItemInstanceInternal()

		for (category in contents)
		{
			for (def in category.second)
			{
				val citem = def.createItem()
				item.children.add(citem)
			}
		}

		return item
	}

	abstract fun createItemInstanceInternal(): I
}

class StructDefinition : AbstractStructDefinition<StructDefinition, StructItem>()
{
	override fun createItemInstanceInternal(): StructItem
	{
		return StructItem(this)
	}

}