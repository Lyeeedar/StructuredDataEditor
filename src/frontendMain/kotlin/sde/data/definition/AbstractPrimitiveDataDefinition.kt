package sde.data.definition

import sde.data.item.AbstractDataItem

abstract class AbstractPrimitiveDataDefinition : AbstractDataDefinition()
{
	abstract fun defaultValue(): Any
	abstract fun defaultValueString(): String
	abstract fun writeToString(item: AbstractDataItem)
	abstract fun loadFromString(data: String)
}