package sde.ui.graph

import org.w3c.dom.CanvasRenderingContext2D
import pl.treksoft.kvision.html.Align
import sde.data.item.DataItem
import sde.ui.*

abstract class AbstractGraphNodeDataItem(val item: DataItem, val node: GraphNode) {
    abstract fun getWidth(context2D: CanvasRenderingContext2D): Double
    abstract fun getHeight(context2D: CanvasRenderingContext2D): Double
    abstract fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox)

    companion object {
        fun create(item: DataItem, node: GraphNode): AbstractGraphNodeDataItem {
            return PreviewGraphNodeDataItem(item, node)
        }
    }
}

class PreviewGraphNodeDataItem(item: DataItem, node: GraphNode) : AbstractGraphNodeDataItem(item, node) {
    private val fontSize = 10

    override fun getWidth(context2D: CanvasRenderingContext2D): Double {
        val bounds = context2D.measureText((fontSize * node.graph.scale).toInt(), item.description)
        return bounds.width
    }

    override fun getHeight(context2D: CanvasRenderingContext2D): Double {
        return fontSize * node.graph.scale
    }

    override fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox) {
        context2D.fillRect(backgroundNormalColour, bounds)
        context2D.strokeRect(borderNormalColour, 1.0, bounds)
        context2D.drawText(fontSize, "white", item.description, bounds, Align.LEFT)
    }
}