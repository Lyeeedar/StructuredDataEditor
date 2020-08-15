package sde.ui.graph

import pl.treksoft.kvision.html.Canvas
import sde.data.DataDocument
import sde.data.item.*
import sde.ui.backgroundReallyDarkColour
import sde.utils.afterInsert

class Graph(val document: DataDocument) : Canvas()
{
    private var inserted = false

    private val actualWidth: Double
        get() = (canvasWidth ?: 0).toDouble()

    private val actualHeight: Double
        get() = (canvasHeight ?: 0).toDouble()


    init {
        afterInsert {
            inserted = true
            redraw()
        }
    }

	fun getGraphNodeItems(item: CompoundDataItem = document.root): Sequence<IGraphNodeItem> {
		return sequence {
			for (child in item.children) {
				if (child is IGraphNodeItem) {
					yield(child as IGraphNodeItem)
				}
				if (child is ReferenceItem && child.createdItem is IGraphNodeItem) {
					yield(child.createdItem as IGraphNodeItem)
				}

				if (child is AbstractCompoundDataItem) {
					for (descendant in getGraphNodeItems(child)) {
						yield(descendant)
					}
				}
			}
		}
	}

	private val nodeCache = HashMap<IGraphNodeItem, GraphNode>()
	fun getGraphNodes(): Sequence<GraphNode> {
		return sequence {
			for (item in getGraphNodeItems()) {
				if (item is CompoundDataItem)
				{
					var existing = nodeCache[item]
					if (existing == null)
					{
						existing = GraphNode(item)
						nodeCache[item] = existing
					}

					yield(existing!!)
				}
			}
		}
	}

    fun redraw() {
        doRedraw()
    }

    fun doRedraw() {
        if (!inserted) return

        context2D.clearRect(0.0, 0.0, actualWidth, actualHeight)

        context2D.fillStyle = backgroundReallyDarkColour
        context2D.fillRect(0.0, 0.0, actualWidth, actualHeight)

	    val graphNodes = getGraphNodes().toList()
	    for (node in graphNodes) {
		    node.draw(context2D)
	    }
    }
}