package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.spinner.ForceType
import pl.treksoft.kvision.form.spinner.SpinnerInput
import sde.data.DataDocument
import sde.data.definition.NumberDefinition

class NumberItem(def: NumberDefinition, document: DataDocument) : AbstractDataItem<NumberDefinition>(def, document)
{
	val value by observable(def.default.toFloat())

	override fun getComponent(): Component
	{
		return SpinnerInput(value, def.minValue, def.maxValue, forceType = if (def.useIntegers) ForceType.ROUND else ForceType.NONE)
	}
}