package sde.data.item

import sde.data.DataDocument
import sde.data.definition.CollectionDefinition
import sde.data.definition.GraphStructDefinition

class GraphStructItem(def: GraphStructDefinition, document: DataDocument) : AbstractStructItem<GraphStructDefinition>(def, document), IGraphNodeItem by GraphNodeItem()
{
	override var nodePositionX: Double by obs(Double.MAX_VALUE, GraphCollectionItem::nodePositionX.name)
		.undoable()
		.get()

	override var nodePositionY: Double by obs(Double.MAX_VALUE, GraphCollectionItem::nodePositionY.name)
		.undoable()
		.get()

	init
	{
		initGraphNode(def, document)
	}

	override fun createContents()
	{
		def.createContents(this, document)
	}
}