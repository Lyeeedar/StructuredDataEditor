package sde.data.definition

import sde.data.DataDocument
import sde.data.item.EnumItem
import sde.util.XElement
import sde.utils.DefinitionLoadException
import sde.utils.parseCategoriedString

class EnumDefinition : AbstractPrimitiveDataDefinition<EnumDefinition, EnumItem>()
{
    val enumValues = HashMap<String, ArrayList<String>>()

    private fun addValue(category: String, value: String) {
        var group = enumValues[category]
        if (group == null) {
            group = ArrayList()
            enumValues[category] = group
        }

        group.add(value)
    }

    override fun doParse(node: XElement) {
        default = node.getAttributeValue("Name", default)

        val enumValues = node.getAttributeValue("EnumValues", "")
        if (enumValues.isNotBlank()) {
            val parsed = enumValues.parseCategoriedString()
            for (category in parsed) {
                for (value in category.value) {
                    addValue(category.key, value)
                }
            }
        }

        val defKey = node.getAttributeValue("Key", "")
        if (defKey.isNotBlank()) {
            registerReference("Key", defKey)
        }
    }

    override fun saveItemInstance(item: EnumItem): XElement {
        return XElement(name, item.value)
    }

    override fun loadItemInstance(document: DataDocument, xml: XElement): EnumItem {
        val item = EnumItem(this, document)
        item.value = xml.value
        return item
    }

    override fun createItemInstance(document: DataDocument): EnumItem {
        return EnumItem(this, document)
    }

    override fun postResolve() {
        val keyDef = getReference<EnumDefinition>("Key")
        if (keyDef != null) {
            for (category in keyDef.enumValues) {
                for (value in category.value) {
                    addValue(category.key, value)
                }
            }
        }

        if (default.isBlank()) {
            default = enumValues.values.first().first()
        }
    }
}