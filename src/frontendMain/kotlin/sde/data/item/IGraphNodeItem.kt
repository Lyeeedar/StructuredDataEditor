package sde.data.item

import sde.data.DataDocument
import sde.data.definition.CategorisedChildren
import sde.data.definition.CollectionDefinition
import sde.data.definition.IGraphNodeDefinition

interface IGraphNodeItem {
    var nodePositionX: Double
    var nodePositionY: Double
	var nodeStore: CollectionItem?

	fun initGraphNode(def: IGraphNodeDefinition, document: DataDocument)
}

class GraphNodeItem : IGraphNodeItem
{
	override var nodePositionX: Double = 0.0
	override var nodePositionY: Double = 0.0

	override var nodeStore: CollectionItem? = null

	override fun initGraphNode(def: IGraphNodeDefinition, document: DataDocument) {
		if (def.flattenData) {
			val nodeStoreDef = CollectionDefinition()
			nodeStoreDef.name = def.nodeStoreName

			val category = CategorisedChildren("", ArrayList(def.nodeDefs.values.toList()))
			nodeStoreDef.contents.add(category)

			val nodeStore = CollectionItem(nodeStoreDef, document)

			this.nodeStore = nodeStore
		}
	}
}