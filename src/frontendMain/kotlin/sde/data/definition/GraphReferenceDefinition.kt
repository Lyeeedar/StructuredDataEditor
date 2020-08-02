package sde.data.definition

import sde.data.DataDocument
import sde.data.item.GraphReferenceItem
import sde.util.XElement

class GraphReferenceDefinition : AbstractReferenceDefinition<GraphReferenceDefinition, GraphReferenceItem>() {
    override fun createItemInstanceInternal(document: DataDocument): GraphReferenceItem {
        TODO("Not yet implemented")
    }

    override fun loadItemInstanceInternal(document: DataDocument): GraphReferenceItem {
        TODO("Not yet implemented")
    }

    override fun saveItemInstanceInternal(item: GraphReferenceItem): XElement {
        TODO("Not yet implemented")
    }
}