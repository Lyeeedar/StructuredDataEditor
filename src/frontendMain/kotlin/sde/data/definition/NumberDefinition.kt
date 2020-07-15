package sde.data.definition

import org.w3c.dom.Node
import sde.utils.getAttributeValue

class NumberDefinition : AbstractPrimitiveDataDefinition()
{
	var minValue = -Float.MAX_VALUE
	var maxValue = Float.MAX_VALUE
	var useIntegers = false

	override fun innerDoParse(node: Node)
	{
		minValue = node.getAttributeValue("Min", minValue)
		maxValue = node.getAttributeValue("Max", maxValue)
		useIntegers = node.getAttributeValue("Type", "Float") == "Int"
	}

}