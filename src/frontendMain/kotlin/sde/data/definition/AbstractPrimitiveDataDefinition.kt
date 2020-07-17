package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.item.AbstractDataItem

abstract class AbstractPrimitiveDataDefinition<D: AbstractPrimitiveDataDefinition<D, I>, I: AbstractDataItem<D>> : AbstractDataDefinition<D, I>()
{
	var default: String = ""

	override fun children(): List<DataDefinition>
	{
		return ArrayList()
	}
}