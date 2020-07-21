package sde.data.definition

import sde.data.DataDocument
import sde.data.item.StringItem
import sde.util.XElement

class StringDefinition : AbstractPrimitiveDataDefinition<StringDefinition, StringItem>()
{
	var maxLength: Int = -1

	override fun doParse(node: XElement)
	{
		maxLength = node.getAttributeValue("MaxLength", maxLength)
		default = node.getAttributeValue("Default", "")
	}

	override fun saveItemInstance(item: StringItem): XElement
	{
		return XElement(name, item.value)
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): StringItem
	{
		val item = StringItem(this, document)
		item.value = xml.value
		return item
	}

	override fun createItemInstance(document: DataDocument): StringItem
	{
		return StringItem(this, document)
	}
}