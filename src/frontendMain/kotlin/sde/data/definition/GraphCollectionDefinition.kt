package sde.data.definition

import sde.data.DataDocument
import sde.data.item.GraphCollectionItem
import sde.util.XElement

class GraphCollectionDefinition : AbstractCollectionDefinition<GraphCollectionDefinition, GraphCollectionItem>(), IGraphNodeDefinition
{
	override var allowReferenceLinks: Boolean = true
	override var allowCircularLinks: Boolean = true
	override var flattenData: Boolean = true
	override var nodeStoreName: String = "Nodes"
	override var background: String = colours["Collection"]!!

	override fun doParseInstanceInternal(node: XElement) {
		allowReferenceLinks = node.getAttributeValue("AllowReferenceLinks", allowReferenceLinks)
		allowCircularLinks = node.getAttributeValue("AllowCircularLinks", allowCircularLinks)
		flattenData = node.getAttributeValue("FlattenData", flattenData)
		nodeStoreName = node.getAttributeValue("NodeStoreName", nodeStoreName)
		background = node.getAttributeValue("Background", background)
	}

	override fun createItemInstanceInternal(document: DataDocument): GraphCollectionItem
	{
		return GraphCollectionItem(this, document)
	}

	override fun loadItemInstanceInternal(document: DataDocument): GraphCollectionItem
	{
		return GraphCollectionItem(this, document)
	}

	override fun saveItemInstanceInternal(item: GraphCollectionItem): XElement
	{
		return XElement(name)
	}
}