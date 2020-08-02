package sde.ui.Graph

import org.w3c.dom.CanvasRenderingContext2D

interface IGraphContents {
    fun draw(context2D: CanvasRenderingContext2D)
}