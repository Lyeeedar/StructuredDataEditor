package sde.ui.graph

import org.w3c.dom.Element
import org.w3c.dom.events.MouseEvent
import pl.treksoft.kvision.core.Cursor
import pl.treksoft.kvision.core.onEvent
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

	var offsetX = 0.0
	var offsetY = 0.0
	var scale = 1.0

	private var mouseX = 0.0
	private var mouseY = 0.0
	private var isMouseOver = false
	var isPanning = false

    init {
        afterInsert {
            inserted = true
            redraw()
        }

		onEvent {
			wheel = {
				onScroll(it.deltaY)

				it.stopPropagation()
				it.preventDefault()
			}
			pointerenter = {
				mouseX = it.offsetX
				mouseY = it.offsetY
				isMouseOver = true
				redraw()
			}
			pointerleave = {
				isMouseOver = false
				redraw()
			}
			pointermove = {
				val deltaX = it.offsetX - mouseX
				val deltaY = it.offsetY - mouseY

				mouseX = it.offsetX
				mouseY = it.offsetY

				onMouseMove(deltaX, deltaY, it.buttons, it.pointerId)
				redraw()
			}
			pointerdown = {
				onMouseDown(it)

				it.stopPropagation()
				it.preventDefault()
			}
			pointerup = {
				onMouseUp()

				it.stopPropagation()
				it.preventDefault()
			}
		}
    }

	private fun getGraphNodeItems(): Sequence<IGraphNodeItem> {
		return sequence {
			if (document.root is IGraphNodeItem) {
				yield(document.root as IGraphNodeItem)

				for (child in getGraphNodeItems(document.root)) {
					yield(child)
				}
			}
		}
	}

	private fun getGraphNodeItems(item: CompoundDataItem): Sequence<IGraphNodeItem> {
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
	private fun getGraphNodes(): Sequence<GraphNode> {
		return sequence {
			for (item in getGraphNodeItems()) {
				if (item is CompoundDataItem)
				{
					yield(getGraphNode(item))
				}
			}
		}
	}
	private fun getGraphNode(item: CompoundDataItem): GraphNode {
		var node = nodeCache[item as IGraphNodeItem]
		if (node == null)
		{
			node = GraphNode(item, this@Graph)
			nodeCache[item] = node
		}

		if (node.graphItem.nodePositionX == Double.MAX_VALUE || node.graphItem.nodePositionY == Double.MAX_VALUE) {
			val parent = getGraphParent(item)
			item.document.undoRedoManager.disableUndoScope {
				if (parent == null) {
					node.graphItem.nodePositionX = 0.0
					node.graphItem.nodePositionY = 0.0
				} else {
					node.graphItem.nodePositionX = parent.nodePositionX + 100
					node.graphItem.nodePositionY = parent.nodePositionY
				}
			}
		}

		return node
	}
	private fun getGraphParent(node: CompoundDataItem): IGraphNodeItem? {
		val parent = node.parent ?: return null
		if (parent is IGraphNodeItem) {
			return parent
		}
		return getGraphParent(parent)
	}

    fun redraw() {
        doRedraw()
    }

    private fun doRedraw() {
        if (!inserted) return

        context2D.clearRect(0.0, 0.0, actualWidth, actualHeight)

        context2D.fillStyle = backgroundReallyDarkColour
        context2D.fillRect(0.0, 0.0, actualWidth, actualHeight)

	    val graphNodes = getGraphNodes().toList()
	    for (node in graphNodes) {
		    node.draw(context2D)
	    }
    }

	private fun onScroll(amount: Double) {
		if (amount < 0) {
			cursor = Cursor.ZOOMIN
		} else {
			cursor = Cursor.ZOOMOUT
		}

		scale -= scale * (amount / 60)

		redraw()
	}

	private fun onMouseDown(mouseEvent: MouseEvent) {
		isPanning = true
	}

	private fun onMouseUp() {
		isPanning = false
	}

	private fun onMouseMove(deltaX: Double, deltaY: Double, button: Short, pointerId: Int) {
		if (isPanning) {
			onPan(deltaX, deltaY, pointerId)
		} else {
			cursor = Cursor.AUTO
		}
	}

	private fun onPan(deltaX: Double, deltaY: Double, pointerId: Int) {
		offsetX += deltaX
		offsetY += deltaY

		cursor = Cursor.ALLSCROLL

		redraw()

		val el = getElement() as? Element
		if (el != null) {
			el.setPointerCapture(pointerId)
		}
	}

}