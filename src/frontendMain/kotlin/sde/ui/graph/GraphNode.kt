package sde.ui.graph

import org.w3c.dom.CanvasRenderingContext2D
import pl.treksoft.kvision.html.Align
import sde.data.definition.AbstractCompoundDefinition
import sde.data.definition.IGraphNodeDefinition
import sde.data.item.*
import sde.ui.*
import kotlin.math.max

class GraphNode(val node: CompoundDataItem) : IGraphContents
{
    private val margin = 2.0
    private val headerFontSize = 14
    private val dataItemCache = HashMap<DataItem, AbstractGraphNodeDataItem>()

	val graphItem: IGraphNodeItem
		get() = node as IGraphNodeItem

    override fun draw(context2D: CanvasRenderingContext2D) {
        val items = getGraphDataItems().toList()
        val bounds = getBounds(context2D, items)

        val headerBounds = context2D.measureText(headerFontSize, node.name)
        headerBounds.x = bounds.x + margin
        headerBounds.y = bounds.y + margin

        // background
        context2D.fillRect(backgroundReallyDarkColour, bounds)
        context2D.strokeRect(borderDarkColour, 1.0, bounds)

        // header
        context2D.drawText(headerFontSize, "white", node.name, headerBounds, Align.CENTER)

        // items
        val x = bounds.x + margin
        var y = bounds.y + margin * 2 + headerBounds.height
        val width = bounds.width - margin * 2

        for (item in items) {
            val height = item.getHeight(context2D)
            val itemBounds = BoundingBox(x, y, width, height)
            item.draw(context2D, itemBounds)

            y += height + margin
        }
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
        val existing = dataItemCache[node]
        if (existing != null) {
            return existing
        }

        val graphItem = AbstractGraphNodeDataItem.create(node)
        dataItemCache[node] = graphItem
        return graphItem
    }

    fun getBounds(context2D: CanvasRenderingContext2D, items: List<AbstractGraphNodeDataItem> = getGraphDataItems().toList()): BoundingBox {
        val itemsWidth = items.map { it.getWidth(context2D) }.max() ?: 0.0
        val itemsHeight = items.sumByDouble { it.getHeight(context2D) + margin }

        val headerBounds = context2D.measureText(headerFontSize, node.name)

        val width = margin * 2 + max(itemsWidth, headerBounds.width)
        val height = margin * 2 + itemsHeight + headerBounds.height + margin

        return BoundingBox(graphItem.nodePositionX, graphItem.nodePositionY, width, height)
    }
}