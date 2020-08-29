package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.DataDocument
import sde.data.item.GraphStructItem
import sde.util.XElement

class GraphStructDefinition : AbstractStructDefinition<GraphStructDefinition, GraphStructItem>(), IGraphNodeDefinition by GraphNodeDefinition()
{
	override fun doParseInstanceInternal(node: XElement)
	{
		parseGraphNode(node)
	}

	protected override fun createItemInstanceInternal(document: DataDocument): GraphStructItem
	{
		return GraphStructItem(this, document)
	}

	protected override fun loadItemInstanceInternal(document: DataDocument, xml: XElement): GraphStructItem
	{
		val item = GraphStructItem(this, document)
		loadGraphNode(xml, item, document)
		return item
	}

	protected override fun saveItemInstanceInternal(item: GraphStructItem): XElement
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