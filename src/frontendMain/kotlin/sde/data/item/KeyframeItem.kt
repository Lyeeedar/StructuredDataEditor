package sde.data.item

import sde.data.DataDocument
import sde.data.definition.KeyframeDefinition

class KeyframeItem(definition: KeyframeDefinition, document: DataDocument) : AbstractStructItem<KeyframeDefinition>(definition, document) {
    override fun createContents() {
        def.createContents(this, document)
    }
}