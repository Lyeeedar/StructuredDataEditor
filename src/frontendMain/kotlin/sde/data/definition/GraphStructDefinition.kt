package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.item.GraphStructItem
import sde.utils.getAttributeValue

class GraphStructDefinition : AbstractStructDefinition<GraphStructDefinition, GraphStructItem>(), IGraphNodeDefinition
{
	override var allowReferenceLinks: Boolean = true
	override var allowCircularLinks: Boolean = true
	override var flattenData: Boolean = true
	override var nodeStoreName: String = "Nodes"
	override var background: String = colours["Struct"]!!

	override fun doParseInstance(node: Element)
	{
		allowReferenceLinks = node.getAttributeValue("AllowReferenceLinks", allowReferenceLinks)
		allowCircularLinks = node.getAttributeValue("AllowCircularLinks", allowCircularLinks)
		flattenData = node.getAttributeValue("FlattenData", flattenData)
		nodeStoreName = node.getAttributeValue("NodeStoreName", nodeStoreName)
		background = node.getAttributeValue("Background", background)
	}

	override fun createItemInstance(): GraphStructItem
	{
		return GraphStructItem()
	}
}