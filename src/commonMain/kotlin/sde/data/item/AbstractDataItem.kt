package sde.data.item

import sde.data.definition.AbstractDataDefinition

abstract class AbstractDataItem
{
	var name: String = ""
	var rawValue: Any? = null
	var rawDef: AbstractDataDefinition? = null
}