package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInputType
import sde.data.DataDocument
import sde.data.definition.StringDefinition

class StringItem(def: StringDefinition, document: DataDocument) : AbstractDataItem<StringDefinition>(def, document)
{
	var value: String by obs(def.default, StringItem::value.name)
		.undoable()
		.get()

	override val description: String
		get() = value

	override fun isDefaultValue(): Boolean
	{
		return value == def.default
	}

	override fun getEditorComponent(): Component
	{
		return TextInput(TextInputType.TEXT, value).apply {
			if (def.maxLength > 0)
			{
				maxlength = def.maxLength
			}

			subscribe {
				this@StringItem.value = it ?: ""
			}
			registerListener(StringItem::value.name) {
				this.value = this@StringItem.value
			}
		}
	}
}