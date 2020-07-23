package sde.data.item

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Image
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.GridPanel
import pl.treksoft.kvision.panel.Side
import pl.treksoft.kvision.panel.gridPanel
import pl.treksoft.kvision.state.observableListOf
import sde.data.DataDocument
import sde.data.definition.DataDefinition
import sde.ui.*
import sde.utils.ObservableClass
import sde.utils.afterInsert
import sde.utils.disableSelection
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

typealias DataItem = AbstractDataItem<*>

abstract class AbstractDataItem<D: DataDefinition>(val def: D, val document: DataDocument): ObservableClass<AbstractDataItem<D>.DataItemObservableBuilder<*>>()
{
	var renderedID = -1
	var depth = 0

	var name: String by obs(def.name, DataItem::name.name)
		.updatesDocument()
		.get()

	val attributes = observableListOf<DataItem>()

	abstract val description: String
	var parent: DataItem? = null
	var isCollectionChild = false

	init {
	    registerListener("") {
			document.scope?.launch {
				var current: AbstractDataItem<*>? = this@AbstractDataItem
				while (current != null) {
					current.raiseEvent(DataItem::description.name)

					delay(100)
					current = current.parent
				}
			}
		}
	}

	// ---------------------------------------- default ------------------------------------------
	fun isDefault(): Boolean {
		for (att in attributes) {
			if (!att.isDefault()) {
				return false
			}
		}

		return isDefaultValue()
	}
	protected abstract fun isDefaultValue(): Boolean

	// ----------------------------------------- util -------------------------------------------
	fun getByPath(path: String): DataItem?
	{
		val pathParts = path.split('.')

		var current: DataItem? = this
		for (part in pathParts) {
			if (current == null) return null

			current = when(part.toLowerCase()) {
				"root" -> current.getRoot()
				"parent" -> current.parent
				else -> {
					var value: DataItem? = null

					val compound = current as? CompoundDataItem
					if (compound != null) {
						value = compound.children.firstOrNull { it.name == part }
					}
					if (value == null) {
						value = attributes.firstOrNull { it.name == part }
					}

					value
				}
			}
		}

		return current
	}

	fun getRoot(): DataItem {
		return parent?.getRoot() ?: this
	}

	// ---------------------------------------- UI ----------------------------------------------
	protected fun isVisible() = document.lastRenderedID == renderedID

	private var editorComponentDiv = Div()
	private var cachedEditorComponent: Component? = null
	fun getEditorComponentCached(): Component
	{
		updateCachedEditorComponent()
		return editorComponentDiv
	}
	private fun updateCachedEditorComponent() {
		if (cachedEditorComponent == null)
		{
			cachedEditorComponent = getEditorComponent()
		}

		editorComponentDiv.removeAll()
		editorComponentDiv.add(cachedEditorComponent!!)
	}
	abstract fun getEditorComponent(): Component

	open fun getEditorRow(sensibleHeaderWidth: Int): Component
	{
		val item = this

		val depth = item.depth * 14 + 14
		val headerWidth = sensibleHeaderWidth - depth

		val borderCol = borderNormalColour
		val backgroundCol = backgroundNormalColour

		return GridPanel(templateColumns = "${headerWidth}px 5px 1fr") {
			marginBottom = CssSize(5, UNIT.px)
			marginLeft = CssSize(depth, UNIT.px)

			val headerDiv = DockPanel {
				borderLeft = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderCol)
				borderTop = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderCol)
				borderBottom = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderCol)
				background = Background(backgroundCol)
				width = CssSize(100, UNIT.perc)
				height = CssSize(100, UNIT.perc)

				if (item is CompoundDataItem && item.children.size > 0)
				{
					if (item.isExpanded)
					{
						add(Image(pl.treksoft.kvision.require("images/OpenArrow.png") as? String), Side.LEFT)
					}
					else
					{
						add(Image(pl.treksoft.kvision.require("images/RightArrow.png") as? String), Side.LEFT)
					}

					onClick {e ->
						item.isExpanded = !item.isExpanded

						e.stopPropagation()
					}
				}

				textBlock(item.name) {
					color = Color(def.textColour)
					marginLeft = CssSize(3, UNIT.px)
				}

				if (item is IRemovable && item.canRemove)
				{
					add(ImageButton(pl.treksoft.kvision.require("images/Remove.png") as? String) {
						onClick {
							item.remove()
						}
					}, Side.RIGHT)
				}
			}
			add(headerDiv, 1, 1)

			val editorDiv = Div {
				borderRight = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderCol)
				borderTop = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderCol)
				borderBottom = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderCol)
				background = Background(backgroundCol)
				width = CssSize(100, UNIT.perc)
				height = CssSize(100, UNIT.perc)

				add(item.getEditorComponentCached())
			}
			add(editorDiv, 3, 1)

			afterInsertHook = {
				val el = getElementJQuery()!!
				el.hover(
						{
							headerDiv.getElementJQuery()!!.css("border-color", mouseOverBorderColour.asString())
							editorDiv.getElementJQuery()!!.css("border-color", mouseOverBorderColour.asString())
						},
						{
							headerDiv.getElementJQuery()!!.css("border-color", borderCol.asString())
							editorDiv.getElementJQuery()!!.css("border-color", borderCol.asString())
						})
			}
		}
	}

	// ----------------------------------- obs -----------------------------------------------------
	final override fun <T> obs(initialValue: T, name: String) = DataItemObservableBuilder<T>(initialValue, name)
	inner class DataItemObservableBuilder<T>(initialValue: T, name: String): AbstractObservableBuilder<T, DataItemObservableBuilder<T>>(initialValue, name)
	{
		var doesUpdateComponent = false
		var doesUpdateDocument = false
		var isUndoable = false

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

		fun undoable(): DataItemObservableBuilder<T>
		{
			isUndoable = true

			return this
		}

		override fun beforeChange(kProperty: KProperty<*>, property: ObservableProperty<T>, oldValue: T, newValue: T): Boolean = true
		override fun afterChange(kProperty: KProperty<*>, property: ObservableProperty<T>, oldValue: T, newValue: T)
		{
			if (doesUpdateComponent) {
				cachedEditorComponent = null

				if (isVisible())
				{
					updateCachedEditorComponent()
				}
			}

			if (doesUpdateDocument && isVisible()) {
				document.updateComponent()
			}

			if (isUndoable)
			{
				document.undoRedoManager.doValueChange(this@AbstractDataItem,
				                                       oldValue, null,
				                                       newValue, null, {
						value, data -> document.undoRedoManager.disableUndoScope {
						property.setValue(property, kProperty, value)
					}
				}, name)
			}
		}
	}
}