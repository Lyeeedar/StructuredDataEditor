package sde.ui

import com.github.snabbdom.VNode
import pl.treksoft.kvision.html.Canvas
import sde.data.item.ColourItem
import sde.data.item.Keyframe
import sde.data.item.TimelineItem

class Timeline(val timelineItem: TimelineItem) : Canvas()
{
	private val possibleValueSteps = arrayOf(10000, 5000, 1000, 500, 100, 50, 10, 5, 1, 0.5, 0.1, 0.05, 0.01, 0.005, 0.001, 0.0005, 0.0001)
	private val trackColours = arrayOf("forestgreen", "darkcyan", "darkviolet", "darkorange")

	private val actualWidth: Double
		get() = (canvasWidth ?: 0).toDouble()

	private val actualHeight: Double
		get() = (canvasHeight ?: 0).toDouble()

	private fun doRedraw() {
		context2D.clearRect(0.0, 0.0, actualWidth, actualHeight)

		context2D.fillStyle = backgroundDarkColour
		context2D.fillRect(0.0, 0.0, actualWidth, actualHeight)

		val pixelsASecond = actualWidth / timelineItem.timelineRange

		val sortedKeyframes = timelineItem.keyframes.sortedBy { it.time.value }.toList()

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

				val thisDrawPos = thisKeyframe.time.value * pixelsASecond + timelineItem.leftPad
				val nextDrawPos = nextKeyframe.time.value * pixelsASecond + timelineItem.leftPad

				val gradient = context2D.createLinearGradient(0.0, 0.0, nextDrawPos - thisDrawPos, 0.0)
				gradient.addColorStop(0.0, "rgb(${thisCol.value})")
				gradient.addColorStop(1.0, "rgb(${nextCol.value})")

				context2D.fillRect(thisDrawPos, drawPos, nextDrawPos - thisDrawPos, lineHeight)
			}

			for (ii in 0 until keyframes.size) {
				val thisKeyframe = keyframes[ii]

				val thisCol = thisKeyframe.colours[i]

				val thisDrawPos = thisKeyframe.time.value * pixelsASecond + timelineItem.leftPad

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

				val thisDrawPos = thisKeyframe.time.value * pixelsASecond + timelineItem.leftPad
				val nextDrawPos = thisKeyframe.time.value * pixelsASecond + timelineItem.leftPad

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

				val thisDrawPos = thisKeyframe.time.value * pixelsASecond + timelineItem.leftPad

				context2D.fillStyle = col
				context2D.fillRect(thisDrawPos-5, thisH - 5, 10.0, 10.0)

				context2D.strokeStyle = borderDarkColour
				context2D.strokeRect(thisDrawPos-5, thisH - 5, 10.0, 10.0)
			}
		}
	}

	private fun drawIndicators(pixelsASecond: Double) {

	}

	private fun drawKeyFrames(keyframes: List<Keyframe>, pixelsASecond: Double) {

	}
}