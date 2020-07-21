package sde.data.definition

import sde.data.DataDocument
import sde.data.item.CommentItem
import sde.util.XElement

class CommentDefinition : AbstractDataDefinition<CommentDefinition, CommentItem>()
{
	init
	{
		name = ""
	}

	override fun children(): List<DataDefinition>
	{
		return ArrayList()
	}

	override fun doParse(node: XElement)
	{

	}

	override fun saveItemInstance(item: CommentItem): XElement
	{
		return XElement()
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): CommentItem
	{
		return CommentItem(document)
	}

	override fun createItemInstance(document: DataDocument): CommentItem
	{
		return CommentItem(document)
	}
}