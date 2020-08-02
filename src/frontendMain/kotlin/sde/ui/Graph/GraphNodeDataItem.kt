package sde.ui.Graph

import org.w3c.dom.CanvasRenderingContext2D
import pl.treksoft.kvision.html.Align
import sde.data.item.DataItem
import sde.ui.*

abstract class AbstractGraphNodeDataItem(val item: DataItem) {
    abstract fun getWidth(context2D: CanvasRenderingContext2D): Double
    abstract fun getHeight(context2D: CanvasRenderingContext2D): Double
    abstract fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox)

    companion object {
        fun create(item: DataItem): AbstractGraphNodeDataItem {
            return PreviewGraphNodeDataItem(item)
        }
    }
}

class PreviewGraphNodeDataItem(item: DataItem) : AbstractGraphNodeDataItem(item) {
    private val fontSize = 10

    override fun getWidth(context2D: CanvasRenderingContext2D): Double {
        val bounds = context2D.measureText(item.description)
        return bounds.width
    }

    override fun getHeight(context2D: CanvasRenderingContext2D): Double {
        return fontSize.toDouble()
    }

    override fun draw(context2D: CanvasRenderingContext2D, bounds: BoundingBox) {
        context2D.fillRect(backgroundNormalColour, bounds)
        context2D.strokeRect(borderNormalColour, 1.0, bounds)
        context2D.drawText(fontSize, "white", item.description, bounds, Align.LEFT)
    }
}