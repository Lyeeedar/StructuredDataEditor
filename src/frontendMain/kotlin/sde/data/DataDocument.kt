package sde.data

import kotlinx.coroutines.*
import org.w3c.dom.HTMLElement
import io.kvision.core.*
import io.kvision.form.select.SelectInput
import io.kvision.form.select.SelectOptGroup
import io.kvision.form.text.TextArea
import io.kvision.form.text.TextAreaInput
import io.kvision.html.*
import io.kvision.panel.*
import io.kvision.require
import io.kvision.toast.Toast
import sde.data.definition.AbstractCompoundDefinition
import sde.data.definition.DataDefinition
import sde.data.item.CompoundDataItem
import sde.data.item.DataItem
import sde.data.item.EnumItem
import sde.data.item.IRemovable
import sde.pages.AbstractPage
import sde.ui.*
import sde.ui.graph.Graph
import sde.util.XAttribute
import sde.util.XElement
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

	val editorDiv = Div().apply {
		id = "Editor"

		width = CssSize(100, UNIT.perc)
		height = CssSize(100, UNIT.perc)
	}
	val dataItemEditor: DataItemEditor by lazy { DataItemEditor(scope ?: MainScope()) }
	val graphEditor: Graph by lazy { Graph(this) }
	var inserted = false

	fun updateEditor() {
		if (!inserted) return

		val editor = when (selectedEditor) {
			"data" -> {
				dataItemEditor.rootItems.clear()
				dataItemEditor.rootItems.add(root)
				dataItemEditor.update()
				dataItemEditor
			}
			"xml" -> {
				TextAreaInput(value = root.def.saveItem(root).toString()).apply {
					width = CssSize(100, UNIT.perc)
					height = CssSize(100, UNIT.perc)
				}
			}
			"graph" -> {
				graphEditor.apply {
					val graph = this
					document.scope?.launch {
						while (true) {
							val el = editorDiv.getElement() as? HTMLElement ?: continue

							if (graph.canvasWidth != el.offsetWidth) {
								graph.canvasWidth = el.offsetWidth - 10
							}
							if (graph.canvasHeight != el.offsetHeight) {
								graph.canvasHeight = el.offsetHeight - 10
							}

							graph.redraw()

							break
						}
					}
				}
			}
			else -> Div()
		}

		editorDiv.removeAll()
		editorDiv.add(editor)
	}

	fun getComponent(): Component
	{
		inserted = true

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

		val div = GridPanel(templateRows = "auto 1fr").apply {
			id = "DataDocument"

			height = CssSize(100, UNIT.perc)
		}
		div.add(buttonsDiv, 1, 1)
		div.add(editorDiv, 1, 2)

		updateEditor()

		return div
	}

	fun loadItem(rootDef: DataDefinition, rootData: XElement): DataItem {
		val root = rootDef.loadItem(this, rootData)

		for (item in root.descendants()) {
			item.postLoad(root)
		}

		return root
	}

	fun save(): XElement {
		val xml = root.def.saveItem(root)
		xml.attributes.add(XAttribute("xmlns:meta", "Editor"))
		return xml
	}
}