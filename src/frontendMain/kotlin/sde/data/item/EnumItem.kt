package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.select.SelectInput
import pl.treksoft.kvision.form.select.SelectOptGroup
import sde.data.DataDocument
import sde.data.definition.EnumDefinition

class EnumItem(definition: EnumDefinition, document: DataDocument) : AbstractDataItem<EnumDefinition>(definition, document)
{
    var value: String by obs(def.default, EnumItem::value.name)
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
        return SelectInput(value = value).apply {
            for (category in def.enumValues.keys.sorted()) {
	            val values = def.enumValues[category]!!
                add(SelectOptGroup(category, values.sorted().map { it to it }))
            }

            subscribe {
                this@EnumItem.value = it ?: ""
            }
            registerListener(StringItem::value.name) {
                this.value = this@EnumItem.value
            }
        }
    }
}