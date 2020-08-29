package sde.data.definition

import sde.data.DataDocument
import sde.data.item.KeyframeItem
import sde.util.XElement

class KeyframeDefinition : AbstractStructDefinition<KeyframeDefinition, KeyframeItem>()
{
    var background = "0,0,0"

    override fun doParseInstanceInternal(node: XElement) {
        background = node.getAttributeValue("Background", background)
    }

    override fun createItemInstanceInternal(document: DataDocument): KeyframeItem {
        return KeyframeItem(this, document)
    }

    override fun loadItemInstanceInternal(document: DataDocument, xml: XElement): KeyframeItem {
        return KeyframeItem(this, document)
    }

    override fun saveItemInstanceInternal(item: KeyframeItem): XElement {
        return XElement(name)
    }
}