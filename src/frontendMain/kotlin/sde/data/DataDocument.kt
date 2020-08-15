package sde.data

import kotlinx.coroutines.*
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.select.SelectInput
import pl.treksoft.kvision.form.select.SelectOptGroup
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.*
import pl.treksoft.kvision.require
import pl.treksoft.kvision.toast.Toast
import sde.data.item.CompoundDataItem
import sde.data.item.DataItem
import sde.data.item.EnumItem
import sde.data.item.IRemovable
import sde.pages.AbstractPage
import sde.ui.*
import sde.ui.graph.Graph
import sde.utils.BasicObservableClass
import sde.utils.UndoRedoManager
import sde.utils.getFileName

class DataDocument(val path: String) : BasicObservableClass()
{
	var name: String = path.getFileName()
	lateinit var root: CompoundDataItem
	lateinit var project: Project
	var scope: CoroutineScope? = null

	val undoRedoManager = UndoRedoManager()

	var selectedEditor: String by obs("data", DataDocument::selectedEditor.name).get()

	val editorDiv = Div()
	val dataItemEditor: DataItemEditor by lazy { DataItemEditor(scope ?: MainScope()) }

	fun updateEditor() {
		val editor = when (selectedEditor) {
			"data" -> {
				dataItemEditor.rootItems.clear()
				dataItemEditor.rootItems.add(root)
				dataItemEditor.update()
				dataItemEditor
			}
			"xml" -> {
				TextArea(value = root.def.saveItem(root).toString())
			}
			"graph" -> {
				Graph(this)
			}
			else -> Div()
		}

		editorDiv.removeAll()
		editorDiv.add(editor)
	}

	fun getComponent(): Component
	{
		val buttonsDiv = HPanel()
		buttonsDiv.add(SelectInput(value = selectedEditor, options = listOf("xml" to "xml", "data" to "data", "graph" to "graph")).apply {
			subscribe {
				this@DataDocument.selectedEditor = it ?: ""

				updateEditor()
			}
			registerListener(DataDocument::selectedEditor.name) {
				this.value = this@DataDocument.selectedEditor
			}
		})

		val div = DockPanel()
		div.add(buttonsDiv, Side.UP)
		div.add(editorDiv)

		updateEditor()

		return div
	}
}