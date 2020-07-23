package sde.data.definition

import sde.data.DataDocument
import sde.data.item.ColourItem
import sde.util.XElement

class ColourDefinition : AbstractPrimitiveDataDefinition<ColourDefinition, ColourItem>()
{
	override fun doParse(node: XElement) {
		default = node.getAttributeValue("Default", "255,255,255")
	}

	override fun createItemInstance(document: DataDocument): ColourItem
	{
		return ColourItem(this, document)
	}

	override fun saveItemInstance(item: ColourItem): XElement
	{
		return XElement(item.name, item.value)
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): ColourItem
	{
		val item = createItemInstance(document)
		item.value = xml.value
		return item
	}
}