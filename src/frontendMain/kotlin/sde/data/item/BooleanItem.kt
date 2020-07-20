package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.check.CheckBox
import sde.data.DataDocument
import sde.data.definition.BooleanDefinition

class BooleanItem(def: BooleanDefinition, document: DataDocument) : AbstractDataItem<BooleanDefinition>(def, document)
{
    var value: Boolean by obs(def.default.toBoolean(), BooleanItem::value.name)
            .undoable()
            .get()

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