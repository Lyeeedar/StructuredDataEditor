package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.item.AbstractDataItem
import sde.utils.getAttributeValue

abstract class AbstractPrimitiveDataDefinition<D: AbstractPrimitiveDataDefinition<D, I>, I: AbstractDataItem<D>> : AbstractDataDefinition<D, I>()
{
	var default: String = ""

	override fun children(): List<AbstractDataDefinition<*, *>>
	{
		return ArrayList()
	}

	override fun doParse(node: Element)
	{
		default = node.getAttributeValue("Default", "")

		innerDoParse(node)
	}
	abstract fun innerDoParse(node: Element)
}