package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.DataDocument
import sde.data.item.NumberItem
import sde.util.XElement
import sde.utils.UndoRedoManager

class NumberDefinition : AbstractPrimitiveDataDefinition<NumberDefinition, NumberItem>()
{
	var minValue = -Float.MAX_VALUE
	var maxValue = Float.MAX_VALUE
	var useIntegers = false

	protected override fun doParse(node: XElement)
	{
		minValue = node.getAttributeValue("Min", minValue)
		maxValue = node.getAttributeValue("Max", maxValue)
		useIntegers = node.getAttributeValue("Type", "Float") == "Int"
		default = node.getAttributeValue("Default", "0")
	}

	protected override fun createItemInstance(document: DataDocument): NumberItem
	{
		return NumberItem(this, document)
	}

	protected override fun saveItemInstance(item: NumberItem): XElement
	{
		return XElement(item.name, item.value.toString())
	}

	protected override fun loadItemInstance(document: DataDocument, xml: XElement): NumberItem
	{
		val item = createItemInstance(document)
		item.value = xml.value.toFloatOrNull() ?: 0f
		return item
	}
}