package sde.data.definition

import org.w3c.dom.Node
import sde.utils.getAttributeValue

abstract class AbstractPrimitiveDataDefinition : AbstractDataDefinition()
{
	var default: Any = ""

	override fun children(): List<AbstractDataDefinition>
	{
		return ArrayList()
	}

	override fun doParse(node: Node)
	{
		default = node.getAttributeValue("Default", "")

		innerDoParse(node)
	}
	abstract fun innerDoParse(node: Node)
}