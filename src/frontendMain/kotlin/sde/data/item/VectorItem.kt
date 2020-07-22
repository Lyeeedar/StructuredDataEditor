package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.form.spinner.ForceType
import pl.treksoft.kvision.form.spinner.spinnerInput
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.panel.HPanel
import sde.data.DataDocument
import sde.data.definition.VectorDefinition

class VectorItem(definition: VectorDefinition, document: DataDocument) : AbstractDataItem<VectorDefinition>(definition, document)
{
	var value1: Float by obs(def.vectorDefault[0], VectorItem::value1.name)
		.undoable()
		.get()
	var value2: Float by obs(def.vectorDefault[1], VectorItem::value2.name)
		.undoable()
		.get()
	var value3: Float by obs(def.vectorDefault[2], VectorItem::value3.name)
		.undoable()
		.get()
	var value4: Float by obs(def.vectorDefault[3], VectorItem::value4.name)
		.undoable()
		.get()

	override val description: String
		get() = "$value1,$value2,$value3,$value4"

	override fun isDefaultValue(): Boolean
	{
		return value1 == def.vectorDefault[0] && value2 == def.vectorDefault[1] && value3 == def.vectorDefault[2] && value4 == def.vectorDefault[3]
	}

	override fun getEditorComponent(): Component
	{
		return HPanel {
			span(def.xName + ": ") {
				margin = CssSize(3, UNIT.px)
			}
			spinnerInput(value1, def.minValue, def.maxValue, decimals = if (def.useIntegers) 0 else 4).apply {
				subscribe {
					this@VectorItem.value1 = this.value!!.toFloat()
				}
				registerListener(VectorItem::value1.name) {
					this.value = this@VectorItem.value1
				}
			}

			span(def.yName + ": ") {
				margin = CssSize(3, UNIT.px)
			}
			spinnerInput(value2, def.minValue, def.maxValue, decimals = if (def.useIntegers) 0 else 4).apply {
				subscribe {
					this@VectorItem.value2 = this.value!!.toFloat()
				}
				registerListener(VectorItem::value2.name) {
					this.value = this@VectorItem.value2
				}
			}

			if (def.numComponents > 2)
			{
				span(def.zName + ": ") {
					margin = CssSize(3, UNIT.px)
				}
				spinnerInput(value3, def.minValue, def.maxValue, decimals = if (def.useIntegers) 0 else 4).apply {
					subscribe {
						this@VectorItem.value3 = this.value!!.toFloat()
					}
					registerListener(VectorItem::value3.name) {
						this.value = this@VectorItem.value3
					}
				}
			}

			if (def.numComponents > 3)
			{
				span(def.wName + ": ") {
					margin = CssSize(3, UNIT.px)
				}
				spinnerInput(value4, def.minValue, def.maxValue, decimals = if (def.useIntegers) 0 else 4).apply {
					subscribe {
						this@VectorItem.value4 = this.value!!.toFloat()
					}
					registerListener(VectorItem::value4.name) {
						this.value = this@VectorItem.value4
					}
				}
			}
		}
	}
}