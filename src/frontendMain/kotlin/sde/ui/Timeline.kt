package sde.ui

import com.github.snabbdom.VNode
import org.w3c.dom.Element
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Canvas
import pl.treksoft.kvision.html.Image
import sde.data.item.ColourItem
import sde.data.item.Keyframe
import sde.data.item.TimelineItem
import sde.utils.afterInsert
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round

class Timeline(val timelineItem: TimelineItem) : Canvas(canvasWidth = 1000, canvasHeight = 50)
{
	private val possibleValueSteps = doubleArrayOf(10000.0, 5000.0, 1000.0, 500.0, 100.0, 50.0, 10.0, 5.0, 1.0, 0.5, 0.1, 0.05, 0.01, 0.005, 0.001, 0.0005, 0.0001)
	private val trackColours = arrayOf("forestgreen", "darkcyan", "darkviolet", "darkorange")

	private val actualWidth: Double
		get() = (canvasWidth ?: 0).toDouble()

	private val actualHeight: Double
		get() = (canvasHeight ?: 0).toDouble()

	private var inserted = false
	private var mouseOverItem: Keyframe? = null

	init
	{
		afterInsert {
			inserted = true
			redraw()
		}
	}

	fun redraw() {
		doRedraw()
	}

	private fun findBestIndicatorStep(): Double {
		for (step in possibleValueSteps) {
			val steps = floor(timelineItem.timelineRange / step)
			if (steps > 5 && step < timelineItem.maxTime) {
				return step
			}
		}

		return possibleValueSteps.last()
	}

	private fun doRedraw() {
		if (!inserted) return

		context2D.clearRect(0.0, 0.0, actualWidth, actualHeight)

		context2D.fillStyle = backgroundDarkColour
		context2D.fillRect(0.0, 0.0, actualWidth, actualHeight)

		val pixelsASecond = actualWidth / timelineItem.timelineRange

		val sortedKeyframes = timelineItem.keyframes.sortedBy { it.time }.toList()

		if (timelineItem.def.contentsMap.size == 1) {
			drawInterpolationPreview(sortedKeyframes, pixelsASecond)
		}

		drawIndicators(pixelsASecond)

		drawKeyFrames(sortedKeyframes, pixelsASecond)
	}

	private fun drawInterpolationPreview(keyframes: List<Keyframe>, pixelsASecond: Double) {
		if (keyframes.size == 0) {
			return
		}

		drawInterpolatedColours(keyframes, pixelsASecond)
		drawInterpolatedNumbers(keyframes, pixelsASecond)
	}

	private fun drawInterpolatedColours(keyframes: List<Keyframe>, pixelsASecond: Double) {
		val numColours = keyframes[0].colours.size
		val linePad = 2
		val lineHeight = 5.0
		val bottomPad = (actualHeight - (lineHeight * numColours + (numColours - 1) * linePad)) / 2
		for (i in 0 until numColours) {
			val drawPos = bottomPad + (lineHeight + linePad) * i

			for (ii in 0 until keyframes.size-1) {
				val thisKeyframe = keyframes[ii]
				val nextKeyframe = keyframes[ii+1]

				val thisCol = thisKeyframe.colours[i]
				val nextCol = nextKeyframe.colours[i]

				val thisDrawPos = thisKeyframe.time * pixelsASecond + timelineItem.leftPad
				val nextDrawPos = nextKeyframe.time * pixelsASecond + timelineItem.leftPad

				val gradient = context2D.createLinearGradient(0.0, 0.0, nextDrawPos - thisDrawPos, 0.0)
				gradient.addColorStop(0.0, "rgb(${thisCol.value})")
				gradient.addColorStop(1.0, "rgb(${nextCol.value})")

				context2D.fillStyle = gradient
				context2D.fillRect(thisDrawPos, drawPos, nextDrawPos - thisDrawPos, lineHeight)
			}

			for (ii in 0 until keyframes.size) {
				val thisKeyframe = keyframes[ii]

				val thisCol = thisKeyframe.colours[i]

				val thisDrawPos = thisKeyframe.time * pixelsASecond + timelineItem.leftPad

				context2D.fillStyle = thisCol.value
				context2D.fillRect(thisDrawPos-5, (drawPos+lineHeight/2) - 5, 10.0, 10.0)

				context2D.strokeStyle = borderDarkColour
				context2D.strokeRect(thisDrawPos-5, (drawPos+lineHeight/2) - 5, 10.0, 10.0)
			}
		}
	}

	private fun drawInterpolatedNumbers(keyframes: List<Keyframe>, pixelsASecond: Double) {
		val numNumbers = keyframes[0].numbers.size
		var min = Float.MAX_VALUE
		var max = -Float.MAX_VALUE

		for (keyframe in keyframes) {
			for (num in keyframe.numbers) {
				if (num.value < min) min = num.value
				if (num.value > max) max = num.value
			}
		}

		for (i in 0 until numNumbers) {
			val col = trackColours[i]

			for (ii in 0 until keyframes.size-1) {
				val thisKeyframe = keyframes[ii]
				val nextKeyframe = keyframes[ii+1]

				val thisNum = thisKeyframe.numbers[i]
				val nextNum = nextKeyframe.numbers[i]

				val thisAlpha = (thisNum.value - min) / (max - min)
				val nextAlpha = (nextNum.value - min) / (max - min)

				val thisH = (actualHeight - 20) - (actualHeight - 25) * thisAlpha
				val nextH = (actualHeight - 20) - (actualHeight - 25) * nextAlpha

				val thisDrawPos = thisKeyframe.time * pixelsASecond + timelineItem.leftPad
				val nextDrawPos = thisKeyframe.time * pixelsASecond + timelineItem.leftPad

				context2D.strokeStyle = col
				context2D.beginPath()
				context2D.moveTo(thisDrawPos, thisH)
				context2D.lineTo(nextDrawPos, nextH)
				context2D.stroke()
			}

			for (ii in 0 until keyframes.size) {
				val thisKeyframe = keyframes[ii]

				val thisNum = thisKeyframe.numbers[i]

				val thisAlpha = (thisNum.value - min) / (max - min)

				val thisH = (actualHeight - 20) - (actualHeight - 25) * thisAlpha

				val thisDrawPos = thisKeyframe.time * pixelsASecond + timelineItem.leftPad

				context2D.fillStyle = col
				context2D.fillRect(thisDrawPos-5, thisH - 5, 10.0, 10.0)

				context2D.strokeStyle = borderDarkColour
				context2D.strokeRect(thisDrawPos-5, thisH - 5, 10.0, 10.0)
			}
		}
	}

	private fun drawIndicators(pixelsASecond: Double) {
		val bestStep = findBestIndicatorStep()
		val indicatorStep = bestStep * pixelsASecond
		var tpos = timelineItem.leftPad.toDouble()

		if (tpos < 0) {
			val remainder = timelineItem.leftPad.absoluteValue - floor(timelineItem.leftPad.absoluteValue / indicatorStep) * indicatorStep
			tpos = -remainder
		}

		while (tpos < actualWidth) {
			var time = round(((tpos - timelineItem.leftPad) / pixelsASecond) / bestStep) * bestStep

			val timeText = time.toString().split("0000")[0]
			val textSize = context2D.measureText(timeText)
			val fontHeight = textSize.actualBoundingBoxAscent + textSize.actualBoundingBoxDescent

			context2D.font = "10px Arial"
			context2D.fillStyle = "white"
			context2D.fillText(timeText, tpos - (textSize.width / 2.0), actualHeight - fontHeight)

			context2D.strokeStyle = borderDarkColour
			context2D.beginPath()
			context2D.moveTo(tpos, 0.0)
			context2D.lineTo(tpos, actualHeight - fontHeight)
			context2D.stroke()

			tpos += indicatorStep

			time = round(((tpos - timelineItem.leftPad) / pixelsASecond) / bestStep) * bestStep
			if (time > timelineItem.maxTime) break

			for (i in 0 until 5) {
				val minorStep = indicatorStep / 6
				val mpos = (tpos - indicatorStep) + i * minorStep + minorStep

				context2D.strokeStyle = borderDarkColour
				context2D.beginPath()
				context2D.moveTo(mpos, 20.0)
				context2D.lineTo(mpos, actualHeight - 20)
				context2D.stroke()
			}
		}
	}

	private fun drawKeyFrames(keyframes: List<Keyframe>, pixelsASecond: Double) {
		for (keyframe in keyframes) {
			var thickness = if (keyframe.isSelected) 2 else 1
			if (keyframe == mouseOverItem) thickness++

			val col = if (keyframe.isSelected) selectionBorderColour else borderLightColour

			val width = getKeyframeWidth(keyframe)

			val preview = keyframe.getImagePreview { redraw() }
			if (preview != null) {
				context2D.fillStyle = "rgb(${keyframe.item.def.background})"
				context2D.fillRect(keyframe.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)

				context2D.drawImage(preview, keyframe.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)

				context2D.strokeStyle = col
				context2D.strokeRect(keyframe.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)
			} else {
				if (timelineItem.def.contentsMap.size > 1) {
					val name = keyframe.item.def.name

					context2D.font = "10px Arial"
					val textSize = context2D.measureText(name)
					val fontHeight = textSize.actualBoundingBoxAscent + textSize.actualBoundingBoxDescent

					context2D.fillStyle = "rgb(${keyframe.item.def.background})"
					context2D.fillRect(keyframe.time * pixelsASecond + timelineItem.leftPad, fontHeight, width, actualHeight - 15 - fontHeight)

					context2D.strokeStyle = col
					context2D.strokeRect(keyframe.time * pixelsASecond + timelineItem.leftPad, fontHeight, width, actualHeight - 15 - fontHeight)

					context2D.fillStyle = "white"
					context2D.fillText(name, max(0.0, (keyframe.time * pixelsASecond + timelineItem.leftPad + width / 2) - (textSize.width / 2.0)), 0.0)
				} else {
					context2D.fillStyle = "rgb(${keyframe.item.def.background})"
					context2D.fillRect(keyframe.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)

					context2D.strokeStyle = col
					context2D.strokeRect(keyframe.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)
				}
			}
		}
	}

	private fun getKeyframeWidth(keyframe: Keyframe): Double {
		val pixelsASecond = actualWidth / timelineItem.timelineRange
		if (keyframe.duration > 0f) return keyframe.duration * pixelsASecond

		val preview = keyframe.getImagePreview { redraw() }

		if (preview != null) return actualHeight - 20

		return 10.0
	}
}