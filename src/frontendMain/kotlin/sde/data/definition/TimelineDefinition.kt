package sde.data.definition

import sde.data.DataDocument
import sde.data.item.TimelineItem
import sde.util.XElement

class TimelineDefinition : AbstractCollectionDefinition<TimelineDefinition, TimelineItem>()
{
	override fun createItemInstanceInternal(document: DataDocument): TimelineItem
	{
		return TimelineItem(this, document)
	}

	override fun loadItemInstanceInternal(document: DataDocument, xml: XElement): TimelineItem
	{
		return TimelineItem(this, document)
	}

	override fun saveItemInstanceInternal(item: TimelineItem): XElement
	{
		return XElement(name)
	}

	override fun doParseInstanceInternal(node: XElement)
	{

	}

}