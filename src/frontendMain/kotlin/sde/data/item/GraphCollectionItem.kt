package sde.data.item

import sde.data.DataDocument
import sde.data.definition.CollectionDefinition
import sde.data.definition.GraphCollectionDefinition

class GraphCollectionItem(definition: GraphCollectionDefinition, document: DataDocument) : AbstractCollectionItem<GraphCollectionDefinition>(definition, document), IGraphNodeItem by GraphNodeItem()
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
}