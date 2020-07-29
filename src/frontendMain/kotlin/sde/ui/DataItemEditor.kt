package sde.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.VPanel
import sde.data.item.CompoundDataItem
import sde.data.item.DataItem

class DataItemEditor(val scope: CoroutineScope) : Div() {
    val rootItems = ArrayList<DataItem>()

    init {
        startChangeWatcher()
    }

    private fun getVisibleItems(): Sequence<DataItem>
    {
        return sequence {
            for (root in rootItems) {

                root.depth = 0
                yield(root)

                if (root is CompoundDataItem) {
                    if (root.isExpanded) {
                        for (item in getVisibleItems(root)) {
                            yield(item)
                        }
                    }
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

    var lastRenderedID = 0

    var updateQueued = false
    fun update()
    {
        updateQueued = true
    }

    private fun startChangeWatcher()
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
        removeAll()

        val visibleItems = getVisibleItems().toList()

        var sensibleHeaderWidth = 150
        for (item in visibleItems)
        {
            val indentation = item.depth * 14
            val nameLength = item.name.length * 8

            val itemWidth = indentation + nameLength + 16 + 25 // expander and padding

            if (itemWidth > sensibleHeaderWidth)
            {
                sensibleHeaderWidth = itemWidth
            }

            item.renderedID = lastRenderedID
        }

        add(VPanel {
            for (item in visibleItems)
            {
                add(item.getEditorRow(sensibleHeaderWidth))
            }
        })
    }
}