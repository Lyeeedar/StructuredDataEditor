package sde.data.definition

import sde.data.DataDocument
import sde.data.item.BooleanItem
import sde.util.XElement

class BooleanDefinition : AbstractPrimitiveDataDefinition<BooleanDefinition, BooleanItem>() {
    override fun doParse(node: XElement) {
		default = node.getAttributeValue("Default", "false")
    }

    override fun createItemInstance(document: DataDocument): BooleanItem
    {
        return BooleanItem(this, document)
    }

    override fun saveItemInstance(item: BooleanItem): XElement
    {
        return XElement(name, item.value.toString())
    }

    override fun loadItemInstance(document: DataDocument, xml: XElement): BooleanItem
    {
        val item = createItemInstance(document)
        item.value = xml.value.toBoolean()
        return item
    }
}