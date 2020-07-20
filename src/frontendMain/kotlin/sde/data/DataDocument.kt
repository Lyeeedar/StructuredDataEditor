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
import sde.ui.*
import sde.utils.UndoRedoManager
import sde.utils.getFileName

class DataDocument(val path: String)
{
	var name: String = path.getFileName()
	lateinit var root: CompoundDataItem
	lateinit var project: Project

	val undoRedoManager = UndoRedoManager()

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

	private fun doUpdateComponent()
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
				add(item.getEditorRow(sensibleHeaderWidth))
			}
		})
	}

	fun getComponent(): Component
	{
		updateComponent()
		return component
	}
}