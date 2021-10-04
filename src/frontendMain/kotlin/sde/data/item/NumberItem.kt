package sde.data.item

import io.kvision.core.Component
import io.kvision.form.spinner.ForceType
import io.kvision.form.spinner.SpinnerInput
import sde.data.DataDocument
import sde.data.definition.NumberDefinition

class NumberItem(def: NumberDefinition, document: DataDocument) : AbstractDataItem<NumberDefinition>(def, document)
{
	var value: Float by obs(def.default.toFloatOrNull() ?: 0f, NumberItem::value.name)
		.undoable()
		.get()

	override val description: String
		get() = value.toString()

	override fun getEditorComponent(): Component
	{
		return SpinnerInput(value, def.minValue, def.maxValue, decimals = if (def.useIntegers) 0 else 4).apply {
			subscribe {
				this@NumberItem.value = this.value!!.toFloat()
			}
			registerListener(NumberItem::value.name) {
				this.value = this@NumberItem.value
			}
		}
	}

	override fun isDefaultValue(): Boolean
	{
		return value == (def.default.toFloatOrNull() ?: 0f)
	}
}