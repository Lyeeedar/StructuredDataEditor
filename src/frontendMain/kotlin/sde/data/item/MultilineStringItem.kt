package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInputType
import sde.data.DataDocument
import sde.data.definition.MultilineStringDefinition

class MultilineStringItem(def: MultilineStringDefinition, document: DataDocument) : AbstractDataItem<MultilineStringDefinition>(def, document)
{
	var value: String by obs(def.default, MultilineStringItem::value.name)
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
			subscribe {
				this@MultilineStringItem.value = it ?: ""
			}
			registerListener(StringItem::value.name) {
				this.value = this@MultilineStringItem.value
			}
		}
	}
}