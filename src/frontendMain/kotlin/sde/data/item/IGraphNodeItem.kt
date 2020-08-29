package sde.data.item

import sde.data.DataDocument
import sde.data.definition.CategorisedChildren
import sde.data.definition.CollectionDefinition
import sde.data.definition.IGraphNodeDefinition
import sde.utils.generateUUID

interface IGraphNodeItem {
	var guid: String
    var nodePositionX: Double
    var nodePositionY: Double
	var nodeStore: CollectionItem?

	fun initGraphNode(def: IGraphNodeDefinition, document: DataDocument)
}

class GraphNodeItem : IGraphNodeItem
{
	override var guid: String
		get() {
			if (guidField == null) {
				guidField = generateUUID()
			}

			return guidField!!
		}
		set(value)
		{
			guidField = value
		}
	var guidField: String? = null

	override var nodePositionX: Double = 0.0
	override var nodePositionY: Double = 0.0

	override var nodeStore: CollectionItem? = null

	override fun initGraphNode(def: IGraphNodeDefinition, document: DataDocument) {
		if (def.flattenData) {
			val nodeStoreDef = CollectionDefinition()
			nodeStoreDef.name = def.nodeStoreName

			val category = CategorisedChildren("", ArrayList(def.nodeDefs.values.toList()))
			nodeStoreDef.contents.add(category)
			nodeStoreDef.contentsMap.putAll(def.nodeDefs)

			val nodeStore = CollectionItem(nodeStoreDef, document)

			this.nodeStore = nodeStore
		}
	}
}