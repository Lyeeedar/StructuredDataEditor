package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.DataDocument
import sde.data.item.NumberItem
import sde.utils.UndoRedoManager
import sde.utils.getAttributeValue

class NumberDefinition : AbstractPrimitiveDataDefinition<NumberDefinition, NumberItem>()
{
	var minValue = -Float.MAX_VALUE
	var maxValue = Float.MAX_VALUE
	var useIntegers = false

	override fun doParse(node: Element)
	{
		minValue = node.getAttributeValue("Min", minValue)
		maxValue = node.getAttributeValue("Max", maxValue)
		useIntegers = node.getAttributeValue("Type", "Float") == "Int"
		default = node.getAttributeValue("Default", "0")
	}

	override fun createItemInstance(document: DataDocument): NumberItem
	{
		return NumberItem(this, document)
	}
}