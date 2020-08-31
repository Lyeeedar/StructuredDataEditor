package sde.ui.graph

import org.w3c.dom.CanvasRenderingContext2D
import pl.treksoft.kvision.html.Align
import sde.data.definition.AbstractCompoundDefinition
import sde.data.definition.IGraphNodeDefinition
import sde.data.item.*
import sde.ui.*
import kotlin.math.max
import kotlin.math.roundToInt

class GraphNode(val node: CompoundDataItem, val graph: Graph) : IGraphContents
{
    private val margin = 2.0
    private val headerFontSize = 14
    private val dataItemCache = HashMap<DataItem, AbstractGraphNodeDataItem>()

	val graphItem: IGraphNodeItem
		get() = node as IGraphNodeItem

    override fun draw(context2D: CanvasRenderingContext2D) {
        val margin = margin * graph.scale
        val headerFontSize = (headerFontSize * graph.scale).toInt()

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
        var y = bounds.y + margin * 3 + headerBounds.height
        val width = bounds.width - margin * 2

        for (item in items) {
            val height = item.getHeight(context2D)
            val itemBounds = BoundingBox(x, y, width, height)
            item.draw(context2D, itemBounds)

            y += height + margin
        }
    }

    fun getItemBounds(context2D: CanvasRenderingContext2D, itemToFind: AbstractGraphNodeDataItem): BoundingBox? {
        val margin = margin * graph.scale
        val headerFontSize = (headerFontSize * graph.scale).toInt()

        val items = getGraphDataItems().toList()
        if (!items.contains(itemToFind)) {
            return null
        }

        val bounds = getBounds(context2D, items)

        val headerBounds = context2D.measureText(headerFontSize, node.name)
        headerBounds.x = bounds.x + margin
        headerBounds.y = bounds.y + margin

        // items
        val x = bounds.x + margin
        var y = bounds.y + margin * 3 + headerBounds.height
        val width = bounds.width - margin * 2

        for (item in items) {
            val height = item.getHeight(context2D)
            val itemBounds = BoundingBox(x, y, width, height)

            if (itemToFind == item) {
                return itemBounds
            }

            y += height + margin
        }

        return null
    }

    fun getDataItems(node: CompoundDataItem = this.node): Sequence<DataItem> {
        return sequence {
            for (child in node.children) {
                yield(child)

                if (child is GraphReferenceItem) {

                }
                else if (child is CompoundDataItem) {
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

        val graphItem = AbstractGraphNodeDataItem.create(node, this)
        dataItemCache[node] = graphItem
        return graphItem
    }

    fun getHeaderBounds(context2D: CanvasRenderingContext2D): BoundingBox {
        val height = getBounds(context2D, emptyList())
        val full = getBounds(context2D)
        full.height = height.height

        return full
    }

    fun getBounds(context2D: CanvasRenderingContext2D, items: List<AbstractGraphNodeDataItem> = getGraphDataItems().toList()): BoundingBox {
        val margin = margin * graph.scale
        val itemsWidth = items.map { it.getWidth(context2D) }.max() ?: 0.0
        val itemsHeight = items.sumByDouble { it.getHeight(context2D) + margin }

        val headerBounds = context2D.measureText((headerFontSize * graph.scale).toInt(), node.name)

        val width = margin * 2 + max(itemsWidth, headerBounds.width)
        val height = margin * 4 + itemsHeight + headerBounds.height + margin

        return BoundingBox(graphItem.nodePositionX*graph.scale + graph.offsetX, graphItem.nodePositionY*graph.scale + graph.offsetY, width, height)
    }
}