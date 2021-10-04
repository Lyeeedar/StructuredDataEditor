package sde.ui.graph

import org.w3c.dom.CanvasRenderingContext2D
import io.kvision.html.Align
import sde.data.item.CommentItem
import sde.data.item.DataItem
import sde.data.item.GraphReferenceItem
import sde.ui.*
import sde.utils.removeTags
import kotlin.math.max

abstract class AbstractGraphNodeDataItem(val item: DataItem, val node: GraphNode) {
    abstract fun getWidth(context2D: CanvasRenderingContext2D): Double
    abstract fun getHeight(context2D: CanvasRenderingContext2D): Double
    abstract fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox)

    companion object {
        fun create(item: DataItem, node: GraphNode): AbstractGraphNodeDataItem {
            if (item is CommentItem) {
                return CommentGraphNodeDataItem(item, node)
            } else if (item is GraphReferenceItem) {
                return LinkGraphNodeDataItem(item, node)
            }

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
        val descBounds = context2D.measureText((fontSize * node.graph.scale).toInt(), item.description.removeTags().take(20))
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
        context2D.drawText(fontSize, "white", item.description.removeTags().take(20), textBounds, Align.RIGHT)
    }
}

class CommentGraphNodeDataItem(item: CommentItem, node: GraphNode) : AbstractGraphNodeDataItem(item, node) {
    private val fontSize = 10
    private val margin = 2

    override fun getWidth(context2D: CanvasRenderingContext2D): Double {
        val item = item as CommentItem
        val margin = margin * node.graph.scale
        val bounds = context2D.measureText((fontSize * node.graph.scale).toInt(), item.value)
        return bounds.width + margin * 2
    }

    override fun getHeight(context2D: CanvasRenderingContext2D): Double {
        val margin = margin * node.graph.scale
        return fontSize * node.graph.scale + margin * 2
    }

    override fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox) {
        val item = item as CommentItem

        val fontSize = (fontSize * node.graph.scale).toInt()
        val margin = margin * node.graph.scale

        val textBounds = BoundingBox(bounds)
        textBounds.x += margin
        textBounds.y += margin
        textBounds.width -= margin * 2
        textBounds.height -= margin * 2

        context2D.drawText(fontSize, "grey", item.value, textBounds, Align.CENTER)
    }

}

class LinkGraphNodeDataItem(item: GraphReferenceItem, node: GraphNode) : AbstractGraphNodeDataItem(item, node) {
    private val fontSize = 10
    private val margin = 2
    private val linkSize = 12

    val link = GraphLink(this, null)

    override fun getWidth(context2D: CanvasRenderingContext2D): Double {
        val margin = margin * node.graph.scale
        val nameBounds = context2D.measureText((fontSize * node.graph.scale).toInt(), item.name)
        return nameBounds.width + linkSize * node.graph.scale * 0.5 + margin * 3
    }

    override fun getHeight(context2D: CanvasRenderingContext2D): Double {
        val margin = margin * node.graph.scale
        return max(fontSize, linkSize) * node.graph.scale + margin * 2
    }

    override fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox) {
        val fontSize = (fontSize * node.graph.scale).toInt()
        val margin = margin * node.graph.scale
        val linkSize = linkSize * node.graph.scale

        context2D.fillRect(backgroundNormalColour, bounds)
        context2D.strokeRect(borderNormalColour, 1.0, bounds)

        val textBounds = BoundingBox(bounds)
        textBounds.x += margin
        textBounds.y += margin
        textBounds.width -= margin * 2
        textBounds.height -= margin * 2

        context2D.drawText(fontSize, item.def.textColour, item.name, textBounds, Align.LEFT)

        val radius = linkSize * 0.5
        val col = if ((item as GraphReferenceItem).createdItem == null) "#cc9900" else "green"
        context2D.fillCircle(col, textBounds.x+textBounds.width, textBounds.y+textBounds.height/2, radius)
        context2D.strokeCircle(borderLightColour, 1.0, textBounds.x+textBounds.width, textBounds.y+textBounds.height/2, radius)
    }
}