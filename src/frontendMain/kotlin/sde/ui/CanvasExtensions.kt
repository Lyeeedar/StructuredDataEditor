package sde.ui

import org.w3c.dom.CanvasImageSource
import org.w3c.dom.CanvasRenderingContext2D
import pl.treksoft.kvision.html.Align

class BoundingBox(var x: Double, var y: Double, var width: Double, var height: Double)

fun CanvasRenderingContext2D.fillRect(colour: dynamic, bounds: BoundingBox) {
    this.fillStyle = colour
    this.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
}

fun CanvasRenderingContext2D.strokeRect(colour: dynamic, thickness: Double, bounds: BoundingBox) {
    this.strokeStyle = colour
    this.lineWidth = thickness
    this.strokeRect(bounds.x, bounds.y, bounds.width, bounds.height)
}

fun CanvasRenderingContext2D.drawImage(imageSource: CanvasImageSource, bounds: BoundingBox) {
    this.drawImage(imageSource, bounds.x, bounds.y, bounds.width, bounds.height)
}

fun CanvasRenderingContext2D.drawText(size: Int, colour: dynamic, text: String, bounds: BoundingBox, align: Align) {
    this.font = "${size}px Arial"
    this.lineWidth = 1.0
    this.fillStyle = colour

    val measured = this.measureText(text)

    when (align) {
        Align.LEFT -> this.fillText(text, bounds.x, bounds.y + bounds.height)
        Align.RIGHT -> this.fillText(text, bounds.x+bounds.width - measured.width, bounds.y + bounds.height)
        else -> this.fillText(text, bounds.x + bounds.width * 0.5 - measured.width * 0.5, bounds.y + bounds.height)
    }
}

fun CanvasRenderingContext2D.measureText(size: Int, text: String): BoundingBox {
    this.font = "${size}px Arial"
    this.lineWidth = 1.0
    val measured = this.measureText(text)

    return BoundingBox(0.0, 0.0, measured.width, size.toDouble())
}