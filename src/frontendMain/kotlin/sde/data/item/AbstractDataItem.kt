package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.state.observableListOf
import sde.data.DataDocument
import sde.data.definition.DataDefinition
import sde.utils.ObservableClass
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias DataItem = AbstractDataItem<*>

abstract class AbstractDataItem<D: DataDefinition>(val def: D, val document: DataDocument): ObservableClass<AbstractDataItem<D>.DataItemObservableBuilder<*>>()
{
	var renderedID = -1
	var depth = 0

	var name: String by obs(def.name)
		.raise(DataItem::name.name)
		.updatesDocument()
		.get()

	val attributes = observableListOf<DataItem>()

	protected fun isVisible() = document.lastRenderedID == renderedID

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

	override fun <T> obs(initialValue: T) = DataItemObservableBuilder<T>(initialValue)

	inner class DataItemObservableBuilder<T>(initialValue: T): AbstractObservableBuilder<T, DataItemObservableBuilder<T>>(initialValue)
	{
		var doesUpdateComponent = false
		var doesUpdateDocument = false

		fun updatesComponent(): DataItemObservableBuilder<T>
		{
			doesUpdateComponent = true

			return this
		}

		fun updatesDocument(): DataItemObservableBuilder<T>
		{
			doesUpdateDocument = true

			return this
		}

		override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean = true
		override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T)
		{
			if (doesUpdateComponent) {
				cachedComponent = null

				if (isVisible())
				{
					updateCachedComponent()
				}
			}

			if (doesUpdateDocument && isVisible()) {
				document.updateComponent()
			}
		}
	}
}