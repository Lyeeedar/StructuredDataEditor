package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.DataDocument
import sde.data.item.AbstractCompoundDataItem
import sde.data.item.AbstractDataItem
import sde.data.item.CommentItem
import sde.data.item.StructItem
import sde.util.XComment
import sde.util.XElement
import sde.utils.DefinitionLoadException

abstract class AbstractStructDefinition<D: AbstractStructDefinition<D, I>, I: AbstractCompoundDataItem<D>> : AbstractCompoundDefinition<D, I>()
{
	var description = ""
	var nullable = true

	init
	{
		textColour = colours["Struct"]!!
	}

	protected override fun doParseInstance(node: XElement)
	{
		description = node.getAttributeValue("Description", description)
		nullable = node.getAttributeValue("Nullable", nullable)
	}

	protected override fun createItemInstance(document: DataDocument): I
	{
		val item = createItemInstanceInternal(document)

		if (!nullable)
		{
			createContents(item, document)
		}

		return item
	}

	fun createContents(item: I, document: DataDocument)
	{
		for (category in contents)
		{
			if (category.first.isNotBlank()) {
				item.children.add(CommentItem(document, category.first))
			}

			for (def in category.second)
			{
				val citem = def.createItem(document)
				item.children.add(citem)
			}
		}
	}
	protected abstract fun createItemInstanceInternal(document: DataDocument): I

	protected override fun loadItemInstance(document: DataDocument, xml: XElement): I
	{
		val item = loadItemInstanceInternal(document)

		if (!nullable || xml.children.size > 0) {

			for (category in contents)
			{
				if (category.first.isNotBlank()) {
					item.children.add(CommentItem(document, category.first))
				}

				for (def in category.second)
				{
					val childEl = xml.getElement(def.name)
					if (childEl != null)
					{
						val citem = def.loadItem(document, childEl)
						item.children.add(citem)
					}
					else
					{
						val citem = def.createItem(document)
						item.children.add(citem)
					}
				}
			}
		}

		return item
	}
	protected abstract fun loadItemInstanceInternal(document: DataDocument): I

	protected override fun saveItemInstance(item: I): XElement
	{
		val xml = saveItemInstanceInternal(item)

		for (child in item.children) {
			if (child.def.skipIfDefault && child.isDefault()) {
				continue
			}

			if (child is CommentItem) {
				xml.children.add(XComment(child.value))
				continue
			}

			val childXml = child.def.saveItem(child)
			xml.children.add(childXml)
		}

		return xml
	}
	protected abstract fun saveItemInstanceInternal(item: I): XElement
}

class StructDefinition : AbstractStructDefinition<StructDefinition, StructItem>()
{
	protected override fun createItemInstanceInternal(document: DataDocument): StructItem
	{
		return StructItem(this, document)
	}

	protected override fun loadItemInstanceInternal(document: DataDocument): StructItem
	{
		return StructItem(this, document)
	}

	protected override fun saveItemInstanceInternal(item: StructItem): XElement
	{
		return XElement(item.name)
	}
}