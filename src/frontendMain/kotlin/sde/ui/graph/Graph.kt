package sde.ui.graph

import pl.treksoft.kvision.html.Canvas
import sde.data.DataDocument
import sde.ui.backgroundReallyDarkColour
import sde.utils.afterInsert

class Graph(val document: DataDocument) : Canvas()
{
    private var inserted = false

    private val actualWidth: Double
        get() = (canvasWidth ?: 0).toDouble()

    private val actualHeight: Double
        get() = (canvasHeight ?: 0).toDouble()


    init {
        afterInsert {
            inserted = true
            redraw()
        }
    }

    fun redraw() {
        doRedraw()
    }

    fun doRedraw() {
        if (!inserted) return

        context2D.clearRect(0.0, 0.0, actualWidth, actualHeight)

        context2D.fillStyle = backgroundReallyDarkColour
        context2D.fillRect(0.0, 0.0, actualWidth, actualHeight)
    }
}