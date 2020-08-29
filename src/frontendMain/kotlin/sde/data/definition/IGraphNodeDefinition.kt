package sde.data.definition

import sde.data.DataDocument
import sde.data.item.IGraphNodeItem
import sde.util.XElement
import sde.utils.DefinitionLoadException

interface IGraphNodeDefinition
{
	var allowReferenceLinks: Boolean
	var allowCircularLinks: Boolean
	var flattenData: Boolean
	var nodeStoreName: String
	var background: String
	val nodeDefs: HashMap<String, DataDefinition>

	fun parseGraphNode(node: XElement)
	fun resolveGraphNode(dataDefinition: DataDefinition)
	fun loadGraphNode(node: XElement, item: IGraphNodeItem, document: DataDocument)
	fun saveGraphNode(node: XElement, item: IGraphNodeItem)
}

class GraphNodeDefinition : IGraphNodeDefinition
{
	override var allowReferenceLinks: Boolean = false
	override var allowCircularLinks: Boolean = false
	override var flattenData: Boolean = false
	override var nodeStoreName: String = "Nodes"
	override var background: String = "rgb(180,156,181)"

	override val nodeDefs = HashMap<String, DataDefinition>()

	override fun parseGraphNode(node: XElement) {
		allowReferenceLinks = node.getAttributeValue("AllowReferenceLinks", allowReferenceLinks)
		allowCircularLinks = node.getAttributeValue("AllowCircularLinks", allowCircularLinks)
		flattenData = node.getAttributeValue("FlattenData", flattenData)
		nodeStoreName = node.getAttributeValue("NodeStoreName", nodeStoreName)
		background = node.getAttributeValue("Background", background)
	}

	override fun loadGraphNode(node: XElement, item: IGraphNodeItem, document: DataDocument) {
		if (flattenData) {
			val nodeStoreEl = node.getElement(nodeStoreName)
			if (nodeStoreEl != null) {
				for (elData in nodeStoreEl.children) {
					val el = elData as? XElement ?: continue
					val def = nodeDefs[el.name] ?: continue

					val newItem = def.loadItem(document, el)
					item.nodeStore?.children?.add(newItem)
				}
			}
		}
	}

	override fun saveGraphNode(node: XElement, item: IGraphNodeItem) {
		if (flattenData) {
			val nodeStore = item.nodeStore ?: return
			val saved = nodeStore.def.saveItem(nodeStore)
			node.children.add(saved)
		}
	}

	override fun resolveGraphNode(dataDefinition: DataDefinition) {
		if (flattenData)
		{
			for (desc in dataDefinition.descendants())
			{
				if (desc is IGraphNodeDefinition)
				{
					nodeDefs[desc.name] = desc
				}
			}

			if (nodeDefs.size == 0) {
				throw DefinitionLoadException("Graph contained no child graph nodes")
			}
		}
	}
}