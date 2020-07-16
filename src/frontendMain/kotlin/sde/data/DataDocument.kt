package sde.data

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.panel.*
import sde.data.item.CompoundDataItem
import sde.data.item.DataItem
import sde.utils.afterInsert
import sde.utils.hover

class DataDocument
{
	var name: String = ""
	lateinit var root: CompoundDataItem

	fun getVisibleItems(): Sequence<DataItem>
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

	fun getVisibleItems(current: CompoundDataItem, depth: Int = 1): Sequence<DataItem>
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

	val component = Div()

	fun updateComponent()
	{
		component.removeAll()

		val visibleItems = getVisibleItems().toList()

		var sensibleHeaderWidth = 150
		for (item in visibleItems)
		{
			val indentation = item.depth * 14
			val nameLength = item.name.length * 20

			val itemWidth = indentation + nameLength + 16 + 50 // expander and padding

			if (itemWidth > sensibleHeaderWidth)
			{
				sensibleHeaderWidth = itemWidth
			}
		}

		component.add(VPanel {
			for (item in visibleItems)
			{
				val depth = item.depth * 14 + 14
				val headerWidth = sensibleHeaderWidth - depth

				gridPanel(templateColumns = "${headerWidth}px 5px 1fr") {
					marginBottom = CssSize(5, UNIT.px)
					marginLeft = CssSize(depth, UNIT.px)

					val headerDiv = Div {
						borderLeft = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						borderTop = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						borderBottom = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.DARKGRAY))
						background = Background(Color.name(Col.GRAY))
						width = CssSize(100, UNIT.perc)
						height = CssSize(100, UNIT.perc)

						onEvent {
							mouseover
						}

						span(item.name)

						if (item is CompoundDataItem)
						{
							onClick {e ->
								item.isExpanded = !item.isExpanded
								updateComponent()

								e.stopPropagation()
							}
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