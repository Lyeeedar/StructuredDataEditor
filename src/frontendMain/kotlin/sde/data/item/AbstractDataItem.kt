package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.state.observableListOf
import sde.data.DataDocument
import sde.data.definition.DataDefinition
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias DataItem = AbstractDataItem<*>

abstract class AbstractDataItem<D: DataDefinition>(val def: D, val document: DataDocument)
{
	var depth = 0

	private val listeners = HashMap<String, MutableList<()->Unit>>()

	var name: String by obs(def.name)
		.raise(DataItem::name.name)
		.updatesDocument()
		.get()

	val attributes = observableListOf<DataItem>()

	fun registerListener(event: String, handler: ()->Unit)
	{
		var listenersBlock = listeners[event]
		if (listenersBlock == null)
		{
			listenersBlock = ArrayList()
			listeners[event] = listenersBlock
		}

		listenersBlock.add(handler)
	}

	fun raiseEvent(event: String)
	{
		val listeners = listeners[event] ?: return
		for (listener in listeners)
		{
			listener.invoke()
		}
	}

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

	fun <T> obs(initialValue: T) = ObservableBuilder<T>(initialValue)

	inner class ObservableBuilder<T>(val initialValue: T)
	{
		val changeEvents: ArrayList<String> = ArrayList<String>().apply { add("") }
		var doesUpdatesComponent = false
		var doesUpdatesDocument = false
		var beforeChangeHandler: ((T,T)->Boolean)? = null
		var afterChangeHandler: ((T,T)->Unit)? = null
		var updateEvents = ArrayList<Pair<String, ()->Unit>>()

		fun raise(name: String): ObservableBuilder<T>
		{
			changeEvents.add(name)

			return this
		}

		fun updatesComponent(): ObservableBuilder<T>
		{
			doesUpdatesComponent = true

			return this
		}

		fun updatesDocument(): ObservableBuilder<T>
		{
			doesUpdatesDocument = true

			return this
		}

		fun beforeChange(handler: (T,T)->Boolean): ObservableBuilder<T>
		{
			beforeChangeHandler = handler

			return this
		}

		fun afterChange(handler: (T,T)->Unit): ObservableBuilder<T>
		{
			afterChangeHandler = handler

			return this
		}

		fun updatedBy(event: String, updateFunc: ()->Unit): ObservableBuilder<T>
		{
			updateEvents.add(Pair(event, updateFunc))

			return this
		}

		fun get(): ReadWriteProperty<Any?, T>
		{
			for (event in updateEvents)
			{
				registerListener(event.first) {
					event.second.invoke()
				}
			}

			return object : ObservableProperty<T>(initialValue) {
				override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean {
					if (oldValue == newValue) {
						return false
					}

					if (beforeChangeHandler != null && !beforeChangeHandler!!.invoke(oldValue, newValue)) {
						return false
					}

					return super.beforeChange(property, oldValue, newValue)
				}

				override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
					if (oldValue == newValue) {
						return
					}

					if (afterChangeHandler != null) {
						afterChangeHandler!!.invoke(oldValue, newValue)
					}

					if (doesUpdatesComponent) {
						cachedComponent = null
						updateCachedComponent()
					}

					if (doesUpdatesDocument) {
						document.updateComponent()
					}

					for (i in 0 until changeEvents.size)
					{
						val event = changeEvents[i]
						raiseEvent(event)
					}
				}
			}
		}
	}
}