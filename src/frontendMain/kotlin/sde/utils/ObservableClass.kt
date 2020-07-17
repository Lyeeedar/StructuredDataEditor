package sde.utils

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ObservableClass<B: ObservableClass<B>.AbstractObservableBuilder<*, *>>
{
	private val listeners = HashMap<String, MutableList<()->Unit>>()

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

	abstract fun <T> obs(initialValue: T, name: String): B

	abstract inner class AbstractObservableBuilder<T, B: AbstractObservableBuilder<T, B>>(val initialValue: T, val name: String)
	{
		val changeEvents: ArrayList<String> = ArrayList<String>().apply { add("") }
		var beforeChangeHandler: ((T, T) -> Boolean)? = null
		var afterChangeHandler: ((T, T) -> Unit)? = null
		var updateEvents = ArrayList<Pair<String, () -> Unit>>()

		init
		{
			changeEvents.add(name)
		}

		fun raise(name: String): B
		{
			changeEvents.add(name)

			return this as B
		}

		fun beforeChange(handler: (T, T) -> Boolean): B
		{
			beforeChangeHandler = handler

			return this as B
		}

		fun afterChange(handler: (T, T) -> Unit): B
		{
			afterChangeHandler = handler

			return this as B
		}

		fun updatedBy(event: String, updateFunc: () -> Unit): B
		{
			updateEvents.add(Pair(event, updateFunc))

			return this as B
		}

		protected abstract fun beforeChange(kPproperty: KProperty<*>, property: ObservableProperty<T>, oldValue: T, newValue: T): Boolean
		protected abstract fun afterChange(kProperty: KProperty<*>, property: ObservableProperty<T>, oldValue: T, newValue: T)

		fun get(): ReadWriteProperty<Any?, T>
		{
			for (event in updateEvents)
			{
				registerListener(event.first) {
					event.second.invoke()
				}
			}

			return object : ObservableProperty<T>(initialValue)
			{
				override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean
				{
					if (oldValue == newValue)
					{
						return false
					}

					if (beforeChangeHandler != null && !beforeChangeHandler!!.invoke(oldValue, newValue))
					{
						return false
					}

					if (!this@AbstractObservableBuilder.beforeChange(property, this, oldValue, newValue))
					{
						return false
					}

					return super.beforeChange(property, oldValue, newValue)
				}

				override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T)
				{
					if (oldValue == newValue)
					{
						return
					}

					if (afterChangeHandler != null)
					{
						afterChangeHandler!!.invoke(oldValue, newValue)
					}

					for (i in 0 until changeEvents.size)
					{
						val event = changeEvents[i]
						raiseEvent(event)
					}

					this@AbstractObservableBuilder.afterChange(property, this, oldValue, newValue)
				}
			}
		}
	}
}