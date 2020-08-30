package sde.ui.graph

import org.w3c.dom.CanvasRenderingContext2D
import pl.treksoft.kvision.html.Align
import sde.data.item.DataItem
import sde.ui.*
import sde.utils.removeTags

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
    private val margin = 2

    override fun getWidth(context2D: CanvasRenderingContext2D): Double {
        val margin = margin * node.graph.scale
        val nameBounds = context2D.measureText((fontSize * node.graph.scale).toInt(), item.name)
        val descBounds = context2D.measureText((fontSize * node.graph.scale).toInt(), item.description.removeTags())
        return nameBounds.width + descBounds.width + margin * 3
    }

    override fun getHeight(context2D: CanvasRenderingContext2D): Double {
        val margin = margin * node.graph.scale
        return fontSize * node.graph.scale + margin * 2
    }

    override fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox) {
        val fontSize = (fontSize * node.graph.scale).toInt()
        val margin = margin * node.graph.scale

        context2D.fillRect(backgroundNormalColour, bounds)
        context2D.strokeRect(borderNormalColour, 1.0, bounds)

        val textBounds = BoundingBox(bounds)
        textBounds.x += margin
        textBounds.y += margin
        textBounds.width -= margin * 2
        textBounds.height -= margin * 2

        context2D.drawText(fontSize, item.def.textColour, item.name, textBounds, Align.LEFT)
        context2D.drawText(fontSize, "white", item.description.removeTags(), textBounds, Align.RIGHT)
    }
}