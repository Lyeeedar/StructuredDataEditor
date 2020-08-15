package sde.data.definition

import sde.data.DataDocument
import sde.data.item.GraphReferenceItem
import sde.data.item.ReferenceItem
import sde.util.XElement

class GraphReferenceDefinition : AbstractReferenceDefinition<GraphReferenceDefinition, GraphReferenceItem>() {
	override fun createItemInstanceInternal(document: DataDocument): GraphReferenceItem
	{
		return GraphReferenceItem(this, document)
	}

	override fun loadItemInstanceInternal(document: DataDocument): GraphReferenceItem
	{
		return GraphReferenceItem(this, document)
	}

	override fun saveItemInstanceInternal(item: GraphReferenceItem): XElement {
		return XElement(name)
	}
}