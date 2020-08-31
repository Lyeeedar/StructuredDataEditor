package sde.ui.graph

import org.w3c.dom.Element
import org.w3c.dom.events.MouseEvent
import pl.treksoft.kvision.core.Cursor
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.html.Canvas
import sde.data.DataDocument
import sde.data.item.*
import sde.ui.backgroundReallyDarkColour
import sde.ui.borderDarkColour
import sde.ui.strokeLine
import sde.utils.afterInsert
import kotlin.math.absoluteValue
import kotlin.math.floor

class Graph(val document: DataDocument) : Canvas()
{
	private val possibleValueSteps = doubleArrayOf(10000.0, 7500.0, 5000.0, 2500.0, 1000.0, 750.0, 500.0, 250.0, 100.0, 75.0, 50.0, 25.0, 10.0, 7.5, 5.0, 2.5, 1.0)

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
	var isDraggingNode = false
	var draggedNode: GraphNode? = null

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

	internal fun getGraphNodeItems(): Sequence<IGraphNodeItem> {
		return sequence {
			if (document.root is IGraphNodeItem) {
				yield(document.root as IGraphNodeItem)

				for (child in document.root.descendants()) {
					if (child is IGraphNodeItem)
					{
						yield(child as IGraphNodeItem)
					}
				}
			}
		}
	}

	private val nodeCache = HashMap<IGraphNodeItem, GraphNode>()
	internal fun getGraphNodes(): Sequence<GraphNode> {
		return sequence {
			for (item in getGraphNodeItems()) {
				if (item is CompoundDataItem)
				{
					yield(getGraphNode(item))
				}
			}
		}
	}
	fun getGraphNode(item: CompoundDataItem): GraphNode {
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

		drawGrid()

	    val graphNodes = getGraphNodes().toList()

		drawLinks(graphNodes)
		drawNodes(graphNodes)
    }

	private fun findBestIndicatorStep(): Double {
		for (step in possibleValueSteps) {
			val steps = floor((actualWidth / scale) / step)
			if (steps > 5) {
				return step
			}
		}

		return possibleValueSteps.last()
	}

	private fun drawGrid() {
		val bestStep = findBestIndicatorStep()
		val indicatorStep = bestStep * scale

		// x
		var tpos = offsetX.rem(indicatorStep)

		while (tpos < actualWidth) {
			context2D.strokeLine(borderDarkColour, 1.0, tpos, 0.0, tpos, actualHeight)

			for (i in 0 until 3) {
				val minorStep = indicatorStep / 4
				val mpos = (tpos - indicatorStep) + i * minorStep + minorStep

				context2D.strokeLine(borderDarkColour, 0.5, mpos, 0.0, mpos, actualHeight)
			}

			tpos += indicatorStep
		}

		for (i in 0 until 3) {
			val minorStep = indicatorStep / 4
			val mpos = (tpos - indicatorStep) + i * minorStep + minorStep

			context2D.strokeLine(borderDarkColour, 0.5, mpos, 0.0, mpos, actualHeight)
		}

		// y
		tpos = offsetY.rem(indicatorStep)

		while (tpos < actualHeight) {
			context2D.strokeLine(borderDarkColour, 1.0, 0.0, tpos, actualWidth, tpos)

			for (i in 0 until 3) {
				val minorStep = indicatorStep / 4
				val mpos = (tpos - indicatorStep) + i * minorStep + minorStep

				context2D.strokeLine(borderDarkColour, 0.5, 0.0, mpos, actualWidth, mpos)
			}

			tpos += indicatorStep
		}

		for (i in 0 until 3) {
			val minorStep = indicatorStep / 4
			val mpos = (tpos - indicatorStep) + i * minorStep + minorStep

			context2D.strokeLine(borderDarkColour, 0.5, 0.0, mpos, actualWidth, mpos)
		}
	}

	private fun drawLinks(graphNodes: List<GraphNode>) {
		for (node in graphNodes) {
			for (item in node.getGraphDataItems()) {
				if (item is LinkGraphNodeDataItem) {
					val link = item.link
					link.update(this)
					link.draw(context2D, node.getItemBounds(context2D, item)!!, this)
				}
			}
		}
	}

	private fun drawNodes(graphNodes: List<GraphNode>) {
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
		for (node in getGraphNodes()) {
			if (node.getHeaderBounds(context2D).inBounds(mouseX, mouseY)) {
				isDraggingNode = true
				draggedNode = node

				return
			}
		}

		isPanning = true
	}

	private fun onMouseUp() {
		endDrag()
	}

	private fun onMouseMove(deltaX: Double, deltaY: Double, button: Short, pointerId: Int) {
		val leftMouseDown = button == 1.toShort()
		if (!leftMouseDown) {
			endDrag()
		}

		if (isPanning) {
			onPan(deltaX, deltaY, pointerId)
		} else if (isDraggingNode) {
			onDragNode(deltaX, deltaY, pointerId, draggedNode!!)
		} else {
			var overNode = false
			for (node in getGraphNodes()) {
				if (node.getHeaderBounds(context2D).inBounds(mouseX, mouseY)) {
					overNode = true
					break
				}
			}

			if (overNode) {
				cursor = Cursor.GRAB
			} else {
				cursor = Cursor.AUTO
			}
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

	private fun onDragNode(deltaX: Double, deltaY: Double, pointerId: Int, node: GraphNode) {
		cursor = Cursor.GRABBING

		node.graphItem.nodePositionX += deltaX / scale
		node.graphItem.nodePositionY += deltaY / scale

		redraw()

		val el = getElement() as? Element
		if (el != null) {
			el.setPointerCapture(pointerId)
		}
	}

	private fun endDrag() {
		isPanning = false
		isDraggingNode = false
		draggedNode = null
	}
}