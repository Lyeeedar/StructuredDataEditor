package sde.utils

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.onClick
import pl.treksoft.kvision.require
import sde.ui.ImageButton
import kotlin.js.Date
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

class UndoRedoDescription
{
	var isUndo: Boolean = false
	var isRedo: Boolean = false
	var description: String = ""
	var countFromCurrent: Int = 0
}

interface IUndoRedoAction
{
	val desc: String
	fun doAction()
	fun undoAction()
}

class UndoRedoAction(override val desc: String, val doFunc: ()->Unit, val undoFunc: ()->Unit) : IUndoRedoAction
{
	override fun doAction()
	{
		doFunc.invoke()
	}

	override fun undoAction()
	{
		undoFunc.invoke()
	}
}

class UndoRedoGroup
{
	val actions = ArrayList<IUndoRedoAction>()
	var lastActionTime: Double = Date.now()

	fun doAction() {
		for (action in actions) {
			action.doAction()
		}
	}

	fun undoAction() {
		for (action in actions.reversed()) {
			action.undoAction()
		}
	}

	val desc: String
		get() = actions.map{ it.desc.replace("\n", "") }.distinct().joinToString(",")
}

class ValueChangeAction<T>(val valueName: String, val keyObj: Any, val setter: (T, Any?)->Unit, var oldVal: T, var newVal: T, var oldData: Any? = null, var newData: Any? = null) : IUndoRedoAction
{
	override val desc: String
		get() = "Change $valueName ($oldVal -> $newVal)"

	fun isChange() = oldVal != newVal

	override fun doAction()
	{
		setter.invoke(newVal, newData)
	}

	override fun undoAction()
	{
		setter.invoke(oldVal, oldData)
	}
}

class UndoRedoManager: ObservableClass<UndoRedoManager.UndoRedoManagerObservableBuilder<*>>()
{
	val groupingTime = 500

	private val undoStack = ArrayList<UndoRedoGroup>()
	private val redoStack = ArrayList<UndoRedoGroup>()

	val canUndo: Boolean
		get() = undoStack.size > 0

	val canRedo: Boolean
		get() = redoStack.size > 0

	private var enableUndoRedo = 0
	private var isInApplyUndo = false

	fun <T> doValueChange(keyObj: Any, prevValue: T, prevData: Any?, newValue: T, newData: Any?, setter: (T, Any?)->Unit, valueName: String)
	{
		if (enableUndoRedo != 0)
		{
			setter.invoke(newValue, newData)
			return
		}

		// attempt to collapse into existing
		if (undoStack.size > 0)
		{
			val lastGroup = undoStack[undoStack.size-1]

			val currentTime = Date.now()
			val diff = currentTime - lastGroup.lastActionTime

			if (diff <= groupingTime)
			{
				for (action in lastGroup.actions)
				{
					if (action is ValueChangeAction<*> && action.keyObj == keyObj && action.valueName == valueName)
					{
						val action = action as ValueChangeAction<T>

						action.newVal = newValue
						action.newData = newData
						action.doAction()

						lastGroup.lastActionTime = currentTime
						return
					}
				}
			}
		}

		val action = ValueChangeAction(valueName, keyObj, setter, prevValue, newValue, prevData, newData)
		addUndoRedoAction(action)
	}

	fun disableUndoScope(scopeContents:()->Unit)
	{
		enableUndoRedo--
		scopeContents.invoke()
		enableUndoRedo++
	}

	fun applyDoUndo(doFunc: () -> Unit, undoFunc: () -> Unit, name: String)
	{
		addUndoRedoAction(UndoRedoAction(name, doFunc, undoFunc))
	}

	fun addUndoRedoAction(action: IUndoRedoAction)
	{
		if (enableUndoRedo != 0)
		{
			action.doAction()
			return
		}

		if (isInApplyUndo) {
			throw Exception("Nested apply undo calls!")
		}

		isInApplyUndo = true

		action.doAction()
		redoStack.clear()

		if (undoStack.size != 0)
		{
			val lastGroup = undoStack[undoStack.size-1]

			val currentTime = Date.now()
			val diff = currentTime - lastGroup.lastActionTime
			if (diff <= groupingTime)
			{
				lastGroup.actions.add(action)
				lastGroup.lastActionTime = currentTime
			}
			else
			{
				val group = UndoRedoGroup()
				group.actions.add(action)
				group.lastActionTime = Date.now()
				undoStack.add(group)
			}
		}
		else
		{
			val group = UndoRedoGroup()
			group.actions.add(action)
			group.lastActionTime = Date.now()
			undoStack.add(group)
		}

		isInApplyUndo = false
	}

	fun undo(count: Int) {
		for (i in 0 until count) {
			undo()
		}
	}

	fun redo(count: Int) {
		for (i in 0 until count)
		{
			redo()
		}
	}

	fun undo() {
		if (undoStack.size > 0)
		{
			isInApplyUndo = true

			val group = undoStack.removeAt(undoStack.size-1)
			group.undoAction()
			redoStack.add(group)

			isInApplyUndo = false
		}
	}

	fun redo() {
		if (redoStack.size > 0)
		{
			isInApplyUndo = true

			val group = redoStack.removeAt(redoStack.size-1)
			group.doAction()
			undoStack.add(group)

			isInApplyUndo = false
		}
	}

	val undoButton: Component by lazy {
		ImageButton(require("images/Undo.png") as? String) {
			onClick {
				undo()
			}

			registerListener("") {
				this.setDisabled(!canUndo)
			}
		}
	}

	val redoButton: Component by lazy {
		ImageButton(require("images/Redo.png") as? String) {
			onClick {
				redo()
			}

			registerListener("") {
				this.setDisabled(!canRedo)
			}
		}
	}

	inner class UndoRedoManagerObservableBuilder<T>(initialValue: T, name: String) : AbstractObservableBuilder<T, UndoRedoManagerObservableBuilder<T>>(initialValue, name)
	{
		override fun beforeChange(kProperty: KProperty<*>, property: ObservableProperty<T>, oldValue: T, newValue: T): Boolean
		{
			return true
		}

		override fun afterChange(kProperty: KProperty<*>, property: ObservableProperty<T>, oldValue: T, newValue: T)
		{

		}
	}
	override fun <T> obs(initialValue: T, name: String): UndoRedoManagerObservableBuilder<*> = UndoRedoManagerObservableBuilder(initialValue, name)
}