package sde.ui.graph

import org.w3c.dom.CanvasRenderingContext2D
import sde.data.item.CompoundDataItem
import sde.data.item.GraphReferenceItem
import sde.ui.BoundingBox
import sde.ui.strokeBezier
import sde.ui.strokeLine

class GraphLink(val src: LinkGraphNodeDataItem, var dst: GraphNode? = null)
{
    fun update(graph: Graph) {
        val item = src.item as GraphReferenceItem

        if (item.createdItem == null) {
            dst = null
            return
        }

        dst = graph.getGraphNode(item.createdItem as CompoundDataItem)
    }

    fun draw(context2D: CanvasRenderingContext2D, itemBounds: BoundingBox, graph: Graph) {
        val x1 = itemBounds.x + itemBounds.width
        val y1 = itemBounds.y+itemBounds.height/2

        val x2 = (dst!!.graphItem.nodePositionX+5)*graph.scale + graph.offsetX
        val y2 = (dst!!.graphItem.nodePositionY+5)*graph.scale + graph.offsetY

        context2D.strokeBezier("lime", 3.0 * graph.scale, x1, y1, x2, y2)
    }
}