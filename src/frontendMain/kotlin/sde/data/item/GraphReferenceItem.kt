package sde.data.item

import sde.data.DataDocument
import sde.data.definition.GraphReferenceDefinition
import sde.data.definition.IGraphNodeDefinition

class GraphReferenceItem(definition: GraphReferenceDefinition, document: DataDocument) : AbstractReferenceItem<GraphReferenceDefinition>(definition, document)
{
	var selectedItemGuid: String? = null

	override fun postLoad(root: DataItem)
	{
		if (selectedItemGuid != null)
		{
			val rootDef = root.def
			if (root is IGraphNodeItem && rootDef is IGraphNodeDefinition && rootDef.flattenData)
			{
				val nodeStore = root.nodeStore ?: return

				for (node in nodeStore.children) {
					if (node is IGraphNodeItem && selectedItemGuid == node.guid) {
						selectedItemGuid = null
						selectedDefinition = node.def
						setCreatedItem(node)
						break
					}
				}
			}
		}
	}
}