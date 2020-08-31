package sde.ui

import org.w3c.dom.CanvasImageSource
import org.w3c.dom.CanvasRenderingContext2D
import pl.treksoft.kvision.html.Align
import kotlin.math.PI

class BoundingBox(var x: Double, var y: Double, var width: Double, var height: Double)
{
    constructor(bounds: BoundingBox): this(bounds.x, bounds.y, bounds.width, bounds.height)

    fun inBounds(x: Double, y: Double): Boolean {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height
    }
}

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

    val drawY = bounds.y + bounds.height - size * 0.2

    when (align) {
        Align.LEFT -> this.fillText(text, bounds.x, drawY)
        Align.RIGHT -> this.fillText(text, bounds.x+bounds.width - measured.width, drawY)
        else -> this.fillText(text, bounds.x + bounds.width * 0.5 - measured.width * 0.5, drawY)
    }
}

fun CanvasRenderingContext2D.measureText(size: Int, text: String): BoundingBox {
    this.font = "${size}px Arial"
    this.lineWidth = 1.0
    val measured = this.measureText(text)

    return BoundingBox(0.0, 0.0, measured.width, size.toDouble())
}

fun CanvasRenderingContext2D.fillCircle(colour: dynamic, x: Double, y: Double, radius: Double) {
    beginPath()
    arc(x, y, radius, 0.0, 2 * PI, false)

    fillStyle = colour
    fill()
}

fun CanvasRenderingContext2D.strokeCircle(colour: dynamic, lineWidth: Double, x: Double, y: Double, radius: Double) {
    beginPath()
    arc(x, y, radius, 0.0, 2 * PI, false)

    this.lineWidth = lineWidth
    strokeStyle = colour
    stroke()
}

fun CanvasRenderingContext2D.strokeLine(colour: dynamic, lineWidth: Double, x1: Double, y1: Double, x2: Double, y2: Double) {
    strokeStyle = colour
    this.lineWidth = lineWidth
    beginPath()
    moveTo(x1, y1)
    lineTo(x2, y2)
    stroke()
}