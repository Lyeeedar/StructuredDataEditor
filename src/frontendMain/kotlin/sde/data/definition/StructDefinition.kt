package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.item.AbstractDataItem
import sde.data.item.StructItem
import sde.utils.DefinitionLoadException
import sde.utils.getAttributeValue

abstract class AbstractStructDefinition<D: AbstractStructDefinition<D, I>, I: AbstractDataItem<D>> : AbstractCompoundDefinition<D, I>()
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

	override fun createItem(): I
	{
		val item = createItemInstance()



		return item
	}

	abstract fun createItemInstance(): I
}

class StructDefinition : AbstractStructDefinition<StructDefinition, StructItem>()
{
	override fun createItemInstance(): StructItem
	{
		return StructItem()
	}

}