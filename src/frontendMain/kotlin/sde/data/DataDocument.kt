package sde.data

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.panel.*
import sde.data.item.CompoundDataItem
import sde.data.item.DataItem

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

		component.add(GridPanel(templateColumns = "5px ${sensibleHeaderWidth}px 5px 1fr") {
			for (i in 0 until visibleItems.size)
			{
				val item = visibleItems[i]
				val depth = item.depth * 14 + 14

				add(GridPanel(templateColumns = "${depth}px 1fr", templateRows = "1fr") {

					add(Div {
						background = Background(Color.name(Col.GRAY))
						width = CssSize(100, UNIT.perc)
						height = CssSize(100, UNIT.perc)

						span(item.name)

						if (item is CompoundDataItem)
						{
							onClick {
								item.isExpanded = !item.isExpanded
								updateComponent()
							}
						}
					}, 2, 0)

				}, 2, i)

				add(Div {
					background = Background(Color.name(Col.GRAY))
					width = CssSize(100, UNIT.perc)
					height = CssSize(100, UNIT.perc)

					add(item.getComponent())
				}, 4, i)
			}
		})
	}

	fun getComponent(): Component
	{
		updateComponent()
		return component
	}
}