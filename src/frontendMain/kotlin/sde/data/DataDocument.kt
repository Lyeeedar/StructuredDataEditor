package sde.data

import kotlinx.coroutines.*
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.*
import pl.treksoft.kvision.require
import pl.treksoft.kvision.toast.Toast
import sde.data.item.CompoundDataItem
import sde.data.item.DataItem
import sde.data.item.IRemovable
import sde.pages.AbstractPage
import sde.ui.*
import sde.utils.UndoRedoManager
import sde.utils.getFileName

class DataDocument(val path: String)
{
	var name: String = path.getFileName()
	lateinit var root: CompoundDataItem
	lateinit var project: Project
	var scope: CoroutineScope? = null

	val undoRedoManager = UndoRedoManager()

	val editor: DataItemEditor by lazy { DataItemEditor(scope ?: MainScope()) }

	fun getComponent(): Component
	{
		val editor = editor
		editor.rootItems.clear()
		editor.rootItems.add(root)
		editor.update()

		return editor
	}
}