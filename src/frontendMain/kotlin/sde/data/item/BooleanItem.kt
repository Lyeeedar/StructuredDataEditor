package sde.data.item

import io.kvision.core.Component
import io.kvision.form.check.CheckBox
import sde.data.DataDocument
import sde.data.definition.BooleanDefinition

class BooleanItem(def: BooleanDefinition, document: DataDocument) : AbstractDataItem<BooleanDefinition>(def, document)
{
    var value: Boolean by obs(def.default.toBoolean(), BooleanItem::value.name)
            .undoable()
            .get()

    override val description: String
        get() = value.toString()

    override fun getEditorComponent(): Component
    {
        return CheckBox(value).apply {
            subscribe {
                this@BooleanItem.value = it
            }
            registerListener(BooleanItem::value.name) {
                this.value = this@BooleanItem.value
            }
        }
    }

    override fun isDefaultValue(): Boolean
    {
        return value == def.default.toBoolean()
    }
}