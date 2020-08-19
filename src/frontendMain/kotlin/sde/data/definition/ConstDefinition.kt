package sde.data.definition

import sde.data.DataDocument
import sde.data.item.ConstItem
import sde.util.XElement

class ConstDefinition : AbstractPrimitiveDataDefinition<ConstDefinition, ConstItem>()
{
	lateinit var value: String

	override fun doParse(node: XElement)
	{
		value = node.value
	}

	override fun saveItemInstance(item: ConstItem): XElement
	{
		return XElement(name, value)
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): ConstItem
	{
		return ConstItem(this, document)
	}

	override fun createItemInstance(document: DataDocument): ConstItem
	{
		return ConstItem(this, document)
	}
}