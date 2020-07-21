package sde.data.item

import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInputType
import sde.data.DataDocument
import sde.data.definition.ColourDefinition
import sde.ui.ColorInput

class ColourItem(def: ColourDefinition, document: DataDocument) : AbstractDataItem<ColourDefinition>(def, document)
{
	var value: String by obs(def.default, ColourItem::value.name)
		.undoable()
		.get()

	override fun isDefaultValue(): Boolean
	{
		return value == def.default
	}

	override fun getEditorComponent(): Component
	{
		return TextInput(TextInputType.COLOR, value).apply {
			subscribe {
				this@ColourItem.value = it ?: "255,255,255,255"
			}
			registerListener(BooleanItem::value.name) {
				this.value = this@ColourItem.value
			}
		}
	}

}