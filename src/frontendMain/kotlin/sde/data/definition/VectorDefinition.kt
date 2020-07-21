package sde.data.definition

import sde.data.DataDocument
import sde.data.item.VectorItem
import sde.util.XElement
import sde.utils.tryGet

class VectorDefinition : AbstractPrimitiveDataDefinition<VectorDefinition, VectorItem>()
{
	val vectorDefault = floatArrayOf(0f, 0f, 0f, 0f)
	var numComponents = 2
	var minValue = -Float.MAX_VALUE
	var maxValue = Float.MAX_VALUE
	var useIntegers = false
	var xName: String = "X"
	var yName: String = "Y"
	var zName: String = "Z"
	var wName: String = "W"

	override fun doParse(node: XElement)
	{
		default = node.getAttributeValue("Default", "0,0,0,0")
		val defaultSplit = default.split(",")
		for (i in 0 until vectorDefault.size) {
			val defaultVal = if (defaultSplit.size > i) defaultSplit[i] else defaultSplit.last()
			vectorDefault[i] = defaultVal.toFloatOrNull() ?: 0f
		}
		numComponents = node.getAttributeValue("NumComponents", numComponents)
		minValue = node.getAttributeValue("Min", minValue)
		maxValue = node.getAttributeValue("Max", maxValue)
		useIntegers = node.getAttributeValue("Type", "Float") == "Int"
		xName = node.getAttributeValue("Name1", xName)
		yName = node.getAttributeValue("Name2", yName)
		zName = node.getAttributeValue("Name3", zName)
		wName = node.getAttributeValue("Name4", wName)
	}

	override fun saveItemInstance(item: VectorItem): XElement
	{
		var output = "${item.value1},${item.value2}"
		if (numComponents > 2) {
			output += ",${item.value3}"
		}
		if (numComponents > 3) {
			output += ",${item.value4}"
		}
		return XElement(name, output)
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): VectorItem
	{
		val item = VectorItem(this, document)

		val vectorSplit = xml.value.split(",")
		item.value1 = vectorSplit.tryGet(0)?.toFloatOrNull() ?: vectorDefault[0]
		item.value2 = vectorSplit.tryGet(1)?.toFloatOrNull() ?: vectorDefault[1]
		item.value3 = vectorSplit.tryGet(2)?.toFloatOrNull() ?: vectorDefault[2]
		item.value4 = vectorSplit.tryGet(3)?.toFloatOrNull() ?: vectorDefault[3]

		return item
	}

	override fun createItemInstance(document: DataDocument): VectorItem
	{
		return VectorItem(this, document)
	}
}