package sde.ui

import com.github.snabbdom.VNode
import org.w3c.dom.Element
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.Cursor
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.Canvas
import pl.treksoft.kvision.html.Image
import sde.data.item.ColourItem
import sde.data.item.Keyframe
import sde.data.item.TimelineItem
import sde.utils.afterInsert
import kotlin.browser.window
import kotlin.math.*

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
	private var mouseX = 0.0
	private var mouseY = 0.0
	private var isPanning = false
	private var lastSelectedItem: Keyframe? = null
	private var resizeItem: Keyframe? = null
	private var startPos: Double = -1.0
	private var isResizing = false
	private var resizingLeft = false
	private var snapLines = ArrayList<Double>()
	private var ctrlDown = false

	init
	{
		afterInsert {
			inserted = true
			redraw()
		}

		onEvent {
			wheel = {
				onScroll(it.deltaY)

				it.stopPropagation()
				it.preventDefault()
			}
			pointerenter = {
				mouseX = it.offsetX
				mouseY = it.offsetY
			}
			pointermove = {
				val deltaX = it.offsetX - mouseX

				mouseX = it.offsetX
				mouseY = it.offsetY

				onMouseMove(deltaX, it.buttons, it.pointerId)
			}
			pointerdown = {
				onMouseDown()
			}
			keydown = {
				if (it.ctrlKey) {
					ctrlDown = true
				}
			}
			keyup = {
				if (it.ctrlKey) {
					ctrlDown = false
				}
			}
		}
	}

	fun redraw() {
		doRedraw()
	}

	// region Drawing

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

		drawIndicators(pixelsASecond)

		if (timelineItem.def.contentsMap.size == 1) {
			drawInterpolationPreview(sortedKeyframes, pixelsASecond)
		}

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

				context2D.fillStyle = "rgb(${thisCol.value})"
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
			val fontHeight = 10

			context2D.font = "10px Arial"
			context2D.fillStyle = "gray"
			context2D.fillText(timeText, tpos - (textSize.width / 2.0), actualHeight - 3)

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
					val fontHeight = 10.0

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

	//endregion

	// region events

	private fun onScroll(amount: Double) {
		if (amount < 0) {
			cursor = Cursor.ZOOMIN
		} else {
			cursor = Cursor.ZOOMOUT
		}

		var pixelsASecond = actualWidth / timelineItem.timelineRange

		val valueUnderCursor = (mouseX - timelineItem.leftPad) / pixelsASecond

		timelineItem.timelineRange += (timelineItem.timelineRange * (amount / 120)).toFloat()

		pixelsASecond = actualWidth / timelineItem.timelineRange
		timelineItem.leftPad = ((valueUnderCursor * pixelsASecond) - mouseX) * -1.0
		if (timelineItem.leftPad > 10) timelineItem.leftPad = 10.0

		for (timeline in timelineItem.timelineGroup) {
			timeline.timelineRange = timelineItem.timelineRange
			timeline.leftPad = timelineItem.leftPad
			timeline.timeline.redraw()
		}
	}

	private fun onMouseDown() {
		val pixelsASecond = actualWidth / timelineItem.timelineRange

		val clickPos = mouseX - timelineItem.leftPad
		val clickItem = getItemAt(clickPos)

		isPanning = false
		isResizing = false

		if (clickItem == null) {
			isPanning = true

			for (timeline in timelineItem.timelineGroup) {
				for (keyframe in timeline.keyframes) {
					keyframe.isSelected = false
				}
			}
		} else {
			clickItem.isSelected = true
			lastSelectedItem = clickItem

			resizeItem = clickItem
			startPos = clickPos

			if (!clickItem.isDurationLocked) {
				var time = clickItem.time * pixelsASecond
				if ((clickItem.endTime * pixelsASecond - clickPos).absoluteValue < 10) {
					isResizing = true
					resizingLeft = false
				} else if ((time - clickPos).absoluteValue < 10) {
					isResizing = true
					resizingLeft = true
				}
			}

			if (!isResizing) {
				val timelineGroup = timelineItem.timelineGroup.toList()
				for (timeline in timelineGroup) {
					for (keyframe in timeline.keyframes) {
						if (keyframe.isSelected) {
							val startOffset = (clickPos / pixelsASecond) - keyframe.time
							val timelineIndex = timelineGroup.indexOf(timeline)
							val dragAction = DragAction(keyframe, keyframe.time.toDouble(), startOffset, timelineIndex)
							draggedActions.add(dragAction)

							if (keyframe == clickItem) {
								draggedAction = dragAction
							}
						}
					}
				}

				isDraggingItems = true
				cursor = Cursor.MOVE
			}

			generateSnapList(clickItem)
		}

		redraw()
	}

	private fun getItemAt(clickPos: Double): Keyframe? {
		val pixelsASecond = actualWidth / timelineItem.timelineRange

		for (keyframe in timelineItem.keyframes) {
			val time = keyframe.time * pixelsASecond
			val diff = clickPos - time

			if (diff >= 0 && diff < getKeyframeWidth(keyframe)) {
				return keyframe
			}
		}

		return null
	}

	private fun generateSnapList(dragged: Keyframe) {
		snapLines.clear()

		for (timeline in timelineItem.timelineGroup) {
			for (keyframe in timeline.keyframes) {
				if (keyframe == dragged || keyframe.isSelected) continue

				val time = keyframe.time.toDouble()
				if (!snapLines.contains(time)) snapLines.add(time)
				if (keyframe.duration > 0f) {
					val time = keyframe.endTime.toDouble()
					if (!snapLines.contains(time)) snapLines.add(time)
				}
			}
		}

		snapLines.sort()
	}

	private fun snap(time: Double): Double {
		var time = time
		if (ctrlDown) {
			val bestStep = findBestIndicatorStep() / 6
			val roundedTime = floor(time / bestStep) * bestStep
			time = roundedTime
		} else {
			val pixelsASecond = actualWidth / timelineItem.timelineRange

			var bestSnapTime = -1.0
			var bestSnapDist = 10.0 / pixelsASecond

			for (line in snapLines) {
				val diff = (line - time).absoluteValue
				if (diff < bestSnapDist) {
					bestSnapDist = diff
					bestSnapTime = line
				}
			}

			if (bestSnapTime > -1) {
				time = bestSnapTime
			}
		}

		return time
	}

	private fun onMouseMove(deltaX: Double, button: Short, pointerId: Int) {
		val leftMouseDown = button == 1.toShort()

		if ((isDraggingItems || isResizing) && !leftMouseDown) {
			endDrag()
		}

		val pixelsASecond = actualWidth / timelineItem.timelineRange
		val clickPos = mouseX - timelineItem.leftPad

		cursor = Cursor.AUTO


		if (leftMouseDown) {
			if (isDraggingItems) {
				val dragItems = draggedActions
				val dragItem = draggedAction!!

				// do time change
				val newTime = clickPos / pixelsASecond - dragItem.actionStartOffset
				var roundedTime = snap(newTime)

				if (dragItem.keyframe.duration > 0f && !ctrlDown) {
					val endTime = newTime + dragItem.keyframe.duration
					val snapped = snap(endTime)
					roundedTime += snapped - endTime
				}

				val diff = roundedTime - dragItem.originalPosition

				for (item in dragItems) {
					item.keyframe.time = (item.originalPosition + diff).toFloat()
				}

				// do timeline change
				val timelineGroup = timelineItem.timelineGroup.toList()
				val currentTimelineIndex = timelineGroup.indexOf(timelineItem)
				if (currentTimelineIndex != timelineGroup.indexOf(dragItem.keyframe.timelineItem)) {
					val idealChange = currentTimelineIndex - dragItem.startTimelineIndex

					// move items
					for (item in dragItems) {
						val itemTimelineIndex = item.startTimelineIndex

						for (i in 0 until idealChange.absoluteValue+1) {
							val signedI = idealChange.sign
							val index = itemTimelineIndex + (idealChange - signedI)
							val targetTimeline = timelineGroup[index]

							if (targetTimeline.def.contentsMap.values.contains(item.keyframe.item.def)) {
								item.keyframe.timelineItem.children.remove(item.keyframe.item)
								targetTimeline.children.add(item.keyframe.item)
							}
						}
					}
				}

				cursor = Cursor.MOVE
			} else if (isPanning) {
				timelineItem.leftPad += deltaX

				if (timelineItem.leftPad > 10) timelineItem.leftPad = 10.0

				cursor = Cursor.ALLSCROLL

				for (timeline in timelineItem.timelineGroup) {
					timeline.leftPad = timelineItem.leftPad
					timeline.timeline.redraw()
				}

				val el = getElement() as? Element
				if (el != null) {
					el.setPointerCapture(pointerId)
				}
			} else if (isResizing) {

				val resizeItem = resizeItem!!
				if (resizingLeft) {
					val newTime = clickPos / pixelsASecond
					var roundedTime = snap(newTime)

					val oldEnd = resizeItem.endTime.toDouble()

					if (roundedTime > oldEnd) roundedTime = oldEnd
					resizeItem.time = roundedTime.toFloat()

					resizeItem.duration = (oldEnd - resizeItem.time).toFloat()
				} else {
					val newTime = clickPos / pixelsASecond
					val roundedTime = snap(newTime)

					resizeItem.duration = (roundedTime - resizeItem.time).toFloat()
				}

				cursor = Cursor.EWRESIZE

				val el = getElement() as? Element
				if (el != null) {
					el.setPointerCapture(pointerId)
				}
			}
		}
		else {
			val item = getItemAt(clickPos)
			mouseOverItem = item

			if (item != null) {
				if (!item.isDurationLocked) {
					val time = item.time * pixelsASecond
					if ((item.endTime * pixelsASecond - clickPos).absoluteValue < 10 || (time - clickPos).absoluteValue < 10) {
						cursor = Cursor.EWRESIZE
					}
				}
			}
		}

		redraw()
	}

	fun endDrag() {
		val wasDragging = isResizing || isDraggingItems
		isResizing = false
		resizeItem = null
		isPanning = false
		isDraggingItems = false
		draggedActions.clear()
		draggedAction = null

		cursor = Cursor.AUTO

		if (wasDragging) {
			for (timeline in timelineItem.timelineGroup) {
				timeline.children.sortBy { timeline.getKeyframe(it)?.time ?: 0f }
			}
		}
	}

	//endregion

	companion object
	{
		var isDraggingItems = false
		val draggedActions = ArrayList<DragAction>()
		var draggedAction: DragAction? = null
	}
}

class DragAction(val keyframe: Keyframe, val originalPosition: Double, val actionStartOffset: Double, val startTimelineIndex: Int)