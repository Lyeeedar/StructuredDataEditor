package sde.ui

import org.w3c.dom.CanvasImageSource
import org.w3c.dom.CanvasRenderingContext2D

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