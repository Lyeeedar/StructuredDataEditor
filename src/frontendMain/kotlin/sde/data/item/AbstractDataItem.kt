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
	var rawValue: Any? by obs(null)

	val attributes = observableListOf<DataItem>()

	abstract fun getComponent(): Component
}