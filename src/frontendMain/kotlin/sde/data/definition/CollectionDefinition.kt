package sde.data.definition

import sde.data.DataDocument
import sde.data.item.*
import sde.util.XComment
import sde.util.XElement

abstract class AbstractCollectionDefinition<D: AbstractCollectionDefinition<D, I>, I: AbstractCollectionItem<D>> : AbstractCompoundDefinition<D, I>()
{
	var childrenAreUnique = false
	val additionalDefs = ArrayList<DataDefinition>()
	var minCount = 0
	var maxCount = Int.MAX_VALUE

	init
	{
		textColour = colours["Collection"]!!
	}

	override fun doParseInstance(node: XElement)
	{
		childrenAreUnique = node.getAttributeValue("ChildrenAreUnique", childrenAreUnique)
		minCount = node.getAttributeValue("MinCount", minCount)
		maxCount = node.getAttributeValue("MaxCount", maxCount)
	}

	override fun doParseChildElement(node: XElement): Boolean
	{
		if (node.name == "AdditionalDefs") {

			for (child in node.children) {
				if (child is XElement) {
					val def = load(child, srcFile)
					additionalDefs.add(def)
				}
			}

			return true
		}

		return false
	}

	protected override fun createItemInstance(document: DataDocument): I
	{
		val item = createItemInstanceInternal(document)

		for (def in additionalDefs) {
			val child = def.createItem(document)
			item.children.add(child)
		}

		if (minCount > 0 && contentsMap.size == 1) {
			for (i in 0 until minCount) {
				item.create()
			}
		}

		return item
	}

	protected abstract fun createItemInstanceInternal(document: DataDocument): I

	protected override fun loadItemInstance(document: DataDocument, xml: XElement): I
	{
		val item = loadItemInstanceInternal(document)

		val remainingChildren = ArrayList(xml.children)

		// create additional defs first
		for (def in additionalDefs) {
			val el = remainingChildren.firstOrNull { it is XElement && it.name == def.name }
			if (el != null && el is XElement) {
				remainingChildren.remove(el)

				val citem = def.loadItem(document, el)
				item.children.add(citem)
			} else {
				val citem = def.createItem(document)
				item.children.add(citem)
			}
		}

		// use remaining to create rest
		for (el in remainingChildren) {
			if (el is XComment) {
				item.children.add(CommentItem(document, el.text))
			} else if (el is XElement) {
				val def = contentsMap[el.name] ?: continue

				val citem = def.loadItem(document, el)
				item.children.add(citem)
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

class CollectionDefinition : AbstractCollectionDefinition<CollectionDefinition, CollectionItem>()
{
	override fun createItemInstanceInternal(document: DataDocument): CollectionItem
	{
		return CollectionItem(this, document)
	}

	override fun loadItemInstanceInternal(document: DataDocument): CollectionItem
	{
		return CollectionItem(this, document)
	}

	override fun saveItemInstanceInternal(item: CollectionItem): XElement
	{
		return XElement(name)
	}
}