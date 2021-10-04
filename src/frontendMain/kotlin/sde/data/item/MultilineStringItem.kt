package sde.data.item

import io.kvision.core.Component
import io.kvision.form.text.TextInput
import io.kvision.form.text.TextInputType
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