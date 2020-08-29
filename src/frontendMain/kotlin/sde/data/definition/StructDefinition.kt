package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.DataDocument
import sde.data.item.*
import sde.util.XComment
import sde.util.XElement
import sde.utils.DefinitionLoadException

abstract class AbstractStructDefinition<D: AbstractStructDefinition<D, I>, I: AbstractStructItem<D>> : AbstractCompoundDefinition<D, I>()
{
	var nullable = true

	init
	{
		textColour = colours["Struct"]!!
	}

	protected override fun doParseInstance(node: XElement)
	{
		nullable = node.getAttributeValue("Nullable", nullable)
		doParseInstanceInternal(node)
	}
	protected abstract fun doParseInstanceInternal(node: XElement)

	protected override fun createItemInstance(document: DataDocument): I
	{
		val item = createItemInstanceInternal(document)

		if (!nullable)
		{
			createContents(item, document)
		}

		return item
	}
	protected abstract fun createItemInstanceInternal(document: DataDocument): I

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

	protected override fun loadItemInstance(document: DataDocument, xml: XElement): I
	{
		val item = loadItemInstanceInternal(document, xml)

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
	protected abstract fun loadItemInstanceInternal(document: DataDocument, xml: XElement): I

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

	override fun doPostResolveInstance() {

	}
}

class StructDefinition : AbstractStructDefinition<StructDefinition, StructItem>()
{
	protected override fun createItemInstanceInternal(document: DataDocument): StructItem
	{
		return StructItem(this, document)
	}

	protected override fun loadItemInstanceInternal(document: DataDocument, xml: XElement): StructItem
	{
		return StructItem(this, document)
	}

	protected override fun saveItemInstanceInternal(item: StructItem): XElement
	{
		return XElement(name)
	}

	override fun doParseInstanceInternal(node: XElement) {

	}
}