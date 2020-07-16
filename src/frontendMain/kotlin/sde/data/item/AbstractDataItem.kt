package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.data.BaseDataComponent
import pl.treksoft.kvision.state.observableListOf
import sde.data.definition.DataDefinition
import sde.utils.UndoRedoManager

typealias DataItem = AbstractDataItem<*>

abstract class AbstractDataItem<D: DataDefinition>(val def: D) : BaseDataComponent()
{
	var depth = 0

	var name: String by obs(def.name)

	val attributes = observableListOf<DataItem>()

	private var cachedComponent: Component? = null
	fun getComponentCached(): Component
	{
		if (cachedComponent == null)
		{
			cachedComponent = getComponent()
		}

		return cachedComponent!!
	}
	abstract fun getComponent(): Component
}