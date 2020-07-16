package sde.data.item

import pl.treksoft.kvision.state.observableListOf
import sde.data.DataDocument
import sde.data.definition.AbstractCompoundDefinition

typealias CompoundDataItem = AbstractCompoundDataItem<*>

abstract class AbstractCompoundDataItem<D: AbstractCompoundDefinition<*, *>>(def: D, document: DataDocument) : AbstractDataItem<D>(def, document)
{
	val children = observableListOf<DataItem>()

	var isExpanded: Boolean by obs(false)
		.raise(CompoundDataItem::isExpanded.name)
		.updatesDocument()
		.get()

	init
	{
		children.onUpdate.add {
			document.updateComponent()

			raiseEvent(CompoundDataItem::children.name)
		}
	}
}