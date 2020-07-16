package sde.data.item

import pl.treksoft.kvision.state.observableListOf
import sde.data.DataDocument
import sde.data.definition.AbstractCompoundDefinition

typealias CompoundDataItem = AbstractCompoundDataItem<*>

abstract class AbstractCompoundDataItem<D: AbstractCompoundDefinition<*, *>>(def: D, document: DataDocument) : AbstractDataItem<D>(def, document)
{
	val children = observableListOf<DataItem>()

	var isExpanded by observable(false)

	init
	{
		children.onUpdate.add {
			document.updateComponent()
		}
	}
}