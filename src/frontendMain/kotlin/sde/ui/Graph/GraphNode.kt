package sde.ui.Graph

import org.w3c.dom.CanvasRenderingContext2D
import sde.data.definition.AbstractCompoundDefinition
import sde.data.definition.AbstractDataDefinition
import sde.data.definition.IGraphNodeDefinition
import sde.data.item.*
import sde.ui.BoundingBox
import sde.ui.backgroundDarkColour
import sde.ui.borderDarkColour

class GraphNode<T, D>(val node: T) : IGraphContents where T : AbstractCompoundDataItem<D>, T: IGraphNodeItem, D: AbstractCompoundDefinition<D, T>, D: IGraphNodeDefinition
{
    private val dataItemCache = HashMap<DataItem, AbstractGraphNodeDataItem>()

    override fun draw(context2D: CanvasRenderingContext2D) {
        val items = getGraphDataItems().toList()
        val bounds = getBounds(items)

        context2D.fillStyle = backgroundDarkColour
        context2D.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)

        context2D.strokeStyle = borderDarkColour
        context2D.lineWidth = thickness.toDouble()
        context2D.strokeRect(bounds.x, bounds.y, bounds.width, bounds.height)
    }

    fun getDataItems(node: CompoundDataItem = this.node): Sequence<DataItem> {
        return sequence {
            for (child in node.children) {
                yield(child)

                if (child is AbstractCompoundDataItem) {
                    for (descendant in getDataItems(child)) {
                        if (descendant is GraphReferenceItem) {
                            yield(descendant)
                        }
                    }
                }
            }
        }
    }

    fun getGraphDataItems(): Sequence<AbstractGraphNodeDataItem> {
        return sequence {
            for (item in getDataItems()) {
                val graphItem = getGraphDataItem(item) ?: continue
                yield(graphItem)
            }
        }
    }

    fun getGraphDataItem(node: DataItem): AbstractGraphNodeDataItem? {
        return null
    }

    fun getBounds(items: List<AbstractGraphNodeDataItem> = getGraphDataItems().toList()): BoundingBox {
        return BoundingBox(node.nodePositionX, node.nodePositionY, items.map { it.getWidth() }.max()!!, items.sumByDouble { it.getHeight() + 2.0 })
    }
}