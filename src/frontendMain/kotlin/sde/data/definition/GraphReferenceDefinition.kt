package sde.data.definition

import sde.data.DataDocument
import sde.data.item.GraphReferenceItem
import sde.data.item.IGraphNodeItem
import sde.data.item.ReferenceItem
import sde.util.XAttribute
import sde.util.XElement

class GraphReferenceDefinition : AbstractReferenceDefinition<GraphReferenceDefinition, GraphReferenceItem>() {
	override fun createItemInstanceInternal(document: DataDocument): GraphReferenceItem
	{
		return GraphReferenceItem(this, document)
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): GraphReferenceItem
	{
		if (xml.children.size == 0 && xml.value.isNotBlank()) {
			// is guid, resolve later

			val item = GraphReferenceItem(this, document)
			item.selectedItemGuid = xml.value
			return item
		}

		return super.loadItemInstance(document, xml)
	}

	override fun loadItemInstanceInternal(document: DataDocument, xml: XElement): GraphReferenceItem
	{
		return GraphReferenceItem(this, document)
	}

	override fun saveItemInstance(item: GraphReferenceItem): XElement
	{
		val citem = item.createdItem
		if (citem is IGraphNodeItem)
		{
			val root = item.document.root
			val rootDef = root.def
			if (rootDef is IGraphNodeDefinition && rootDef.flattenData)
			{
				val el = XElement(name, citem.guid)
				el.attributes.add(XAttribute("meta:RefKey", citem.def.name))
				return el
			}
		}

		return super.saveItemInstance(item)
	}

	override fun saveItemInstanceInternal(item: GraphReferenceItem): XElement {
		return XElement(name)
	}
}