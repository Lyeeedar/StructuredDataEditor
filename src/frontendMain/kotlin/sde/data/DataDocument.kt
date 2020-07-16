package sde.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.*
import pl.treksoft.kvision.require
import sde.data.item.CompoundDataItem
import sde.data.item.DataItem
import sde.data.item.IRemovable
import sde.ui.ImageButton
import sde.ui.imageButton
import sde.utils.afterInsert
import sde.utils.hover
import kotlin.browser.document

class DataDocument
{
	var name: String = ""
	lateinit var root: CompoundDataItem

	private fun getVisibleItems(): Sequence<DataItem>
	{
		return sequence {
			root.depth = 0
			yield(root)

			if (root.isExpanded)
			{
				for (item in getVisibleItems(root))
				{
					yield(item)
				}
			}
		}
	}

	private fun getVisibleItems(current: CompoundDataItem, depth: Int = 1): Sequence<DataItem>
	{
		return sequence {
			for (child in current.children)
			{
				child.depth = depth
				yield(child)

				if (child is CompoundDataItem && child.isExpanded)
				{
					for (item in getVisibleItems(child, depth+1))
					{
						yield(item)
					}
				}
			}
		}
	}

	private val component = Div()
	var lastRenderedID = 0

	var updateQueued = false
	fun updateComponent()
	{
		updateQueued = true
	}

	fun startChangeWatcher(scope: CoroutineScope)
	{
		scope.launch {
			while (true)
			{
				delay(250)

				if (updateQueued)
				{
					updateQueued = false

					doUpdateComponent()
				}
			}
		}
	}

	fun doUpdateComponent()
	{
		lastRenderedID++
		component.removeAll()

		val visibleItems = getVisibleItems().toList()

		var sensibleHeaderWidth = 150
		for (item in visibleItems)
		{
			val indentation = item.depth * 14
			val nameLength = item.name.length * 10

			val itemWidth = indentation + nameLength + 16 + 50 // expander and padding

			if (itemWidth > sensibleHeaderWidth)
			{
				sensibleHeaderWidth = itemWidth
			}

			item.renderedID = lastRenderedID
		}

		component.add(VPanel {
			for (item in visibleItems)
			{
				val depth = item.depth * 14 + 14
				val headerWidth = sensibleHeaderWidth - depth

				gridPanel(templateColumns = "${headerWidth}px 5px 1fr") {
					marginBottom = CssSize(5, UNIT.px)
					marginLeft = CssSize(depth, UNIT.px)

					val headerDiv = DockPanel {
						borderLeft = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						borderTop = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						borderBottom = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						background = Background(Color.name(Col.GRAY))
						width = CssSize(100, UNIT.perc)
						height = CssSize(100, UNIT.perc)

						if (item is CompoundDataItem && item.children.size > 0)
						{
							if (item.isExpanded)
							{
								add(Image(require("images/OpenArrow.png") as? String), Side.LEFT)
							}
							else
							{
								add(Image(require("images/RightArrow.png") as? String), Side.LEFT)
							}

							onClick {e ->
								item.isExpanded = !item.isExpanded

								e.stopPropagation()
							}
						}

						span(item.name)

						if (item is IRemovable && item.canRemove)
						{
							add(ImageButton(require("images/Remove.png") as? String) {
								onClick {
									item.remove()
								}
							}, Side.RIGHT)
						}
					}
					add(headerDiv, 1, 1)

					val editorDiv = Div {
						borderRight = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						borderTop = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						borderBottom = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						background = Background(Color.name(Col.GRAY))
						width = CssSize(100, UNIT.perc)
						height = CssSize(100, UNIT.perc)

						add(item.getComponentCached())
					}
					add(editorDiv, 3, 1)

					afterInsertHook = {
						val el = getElementJQuery()!!
						el.hover(
							{
								headerDiv.getElementJQuery()!!.css("border-color", "green")
								editorDiv.getElementJQuery()!!.css("border-color", "green")
							},
							{
								headerDiv.getElementJQuery()!!.css("border-color", "darkgray")
								editorDiv.getElementJQuery()!!.css("border-color", "darkgray")
							})
					}
				}
			}
		})
	}

	fun getComponent(): Component
	{
		updateComponent()
		return component
	}
}