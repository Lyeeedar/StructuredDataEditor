package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.spinner.ForceType
import pl.treksoft.kvision.form.spinner.SpinnerInput
import sde.data.definition.NumberDefinition

class NumberItem(def: NumberDefinition) : AbstractDataItem<NumberDefinition>(def)
{
	override fun getComponent(): Component
	{
		val value = rawValue.toString().toFloat()

		return SpinnerInput(value, def.minValue, def.maxValue, forceType = if (def.useIntegers) ForceType.ROUND else ForceType.NONE).apply {
			subscribe {
				rawValue = it
			}
		}
	}
}