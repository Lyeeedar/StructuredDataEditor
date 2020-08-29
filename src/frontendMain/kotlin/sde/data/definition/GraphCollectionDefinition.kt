package sde.data.definition

import sde.data.DataDocument
import sde.data.item.GraphCollectionItem
import sde.util.XElement

class GraphCollectionDefinition : AbstractCollectionDefinition<GraphCollectionDefinition, GraphCollectionItem>(), IGraphNodeDefinition by GraphNodeDefinition()
{
	override fun doParseInstanceInternal(node: XElement)
	{
		parseGraphNode(node)
	}

	override fun createItemInstanceInternal(document: DataDocument): GraphCollectionItem
	{
		return GraphCollectionItem(this, document)
	}

	override fun loadItemInstanceInternal(document: DataDocument, xml: XElement): GraphCollectionItem
	{
		val item = GraphCollectionItem(this, document)
		loadGraphNode(xml, item, document)
		return item
	}

	override fun saveItemInstanceInternal(item: GraphCollectionItem): XElement
	{
		val node = XElement(name)
		saveGraphNode(node, item)
		return node
	}

	override fun doPostResolveInstance()
	{
		resolveGraphNode(this)
	}
}