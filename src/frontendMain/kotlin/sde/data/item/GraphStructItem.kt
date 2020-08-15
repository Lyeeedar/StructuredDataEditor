package sde.data.item

import sde.data.DataDocument
import sde.data.definition.GraphStructDefinition

class GraphStructItem(def: GraphStructDefinition, document: DataDocument) : AbstractStructItem<GraphStructDefinition>(def, document), IGraphNodeItem
{
	override var nodePositionX: Double by obs(0.0, GraphCollectionItem::nodePositionX.name)
		.undoable()
		.get()

	override var nodePositionY: Double by obs(0.0, GraphCollectionItem::nodePositionY.name)
		.undoable()
		.get()

	override fun createContents()
	{
		def.createContents(this, document)
	}
}