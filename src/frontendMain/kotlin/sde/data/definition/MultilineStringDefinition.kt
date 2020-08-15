package sde.data.definition

import sde.data.DataDocument
import sde.data.item.MultilineStringItem
import sde.util.XElement

class MultilineStringDefinition : AbstractPrimitiveDataDefinition<MultilineStringDefinition, MultilineStringItem>()
{
	override fun doParse(node: XElement)
	{

	}

	override fun saveItemInstance(item: MultilineStringItem): XElement
	{
		return XElement(name)
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): MultilineStringItem
	{
		return MultilineStringItem(this, document)
	}

	override fun createItemInstance(document: DataDocument): MultilineStringItem
	{
		return MultilineStringItem(this, document)
	}
}