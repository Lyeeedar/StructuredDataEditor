package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.data.BaseDataComponent
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.state.observableListOf
import sde.data.DataDocument
import sde.data.definition.DataDefinition
import sde.utils.UndoRedoManager
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias DataItem = AbstractDataItem<*>

abstract class AbstractDataItem<D: DataDefinition>(val def: D, val document: DataDocument) : BaseDataComponent()
{
	var depth = 0

	var name: String by observable(def.name)

	val attributes = observableListOf<DataItem>()

	private var componentDiv = Div()
	private var cachedComponent: Component? = null
	fun getComponentCached(): Component
	{
		updateCachedComponent()
		return componentDiv
	}
	private fun updateCachedComponent() {
		if (cachedComponent == null)
		{
			cachedComponent = getComponent()
		}

		componentDiv.removeAll()
		componentDiv.add(cachedComponent!!)
	}
	abstract fun getComponent(): Component

	fun <T> observable(initialValue: T, afterChange: ((T, T)->Unit)? = null): ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
		override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
			container?.update()
			cachedComponent = null
			updateCachedComponent()
			afterChange?.invoke(oldValue, newValue)
		}
	}
}