package sde.data.item

import pl.treksoft.kvision.state.observableListOf
import sde.data.definition.AbstractCompoundDefinition

typealias CompoundDataItem = AbstractCompoundDataItem<*>

abstract class AbstractCompoundDataItem<D: AbstractCompoundDefinition<*, *>>(def: D) : AbstractDataItem<D>(def)
{
	val children = observableListOf<DataItem>()

	var isExpanded by obs(false)
}