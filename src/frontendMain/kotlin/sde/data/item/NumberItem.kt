package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.spinner.ForceType
import pl.treksoft.kvision.form.spinner.SpinnerInput
import sde.data.DataDocument
import sde.data.definition.NumberDefinition

class NumberItem(def: NumberDefinition, document: DataDocument) : AbstractDataItem<NumberDefinition>(def, document)
{
	var value: Float by obs(def.default.toFloat(), NumberItem::value.name)
		.undoable()
		.get()

	override fun getEditorComponent(): Component
	{
		return SpinnerInput(value, def.minValue, def.maxValue, forceType = if (def.useIntegers) ForceType.ROUND else ForceType.NONE).apply {
			subscribe {
				this@NumberItem.value = this.value!!.toFloat()
			}
			registerListener(NumberItem::value.name) {
				this.value = this@NumberItem.value
			}
		}
	}
}