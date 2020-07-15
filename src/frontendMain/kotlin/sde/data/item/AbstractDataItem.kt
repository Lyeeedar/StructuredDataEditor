package sde.data.item

import pl.treksoft.kvision.data.BaseDataComponent
import pl.treksoft.kvision.state.observableListOf
import sde.data.definition.AbstractDataDefinition
import sde.utils.UndoRedoManager

abstract class AbstractDataItem<D: AbstractDataDefinition<*, *>> : BaseDataComponent()
{
	lateinit var undoRedo: UndoRedoManager
	lateinit var def: D

	var name: String by obs("")
	var rawValue: Any? by obs(null)

	val attributes = observableListOf<AbstractDataItem<*>>()
}