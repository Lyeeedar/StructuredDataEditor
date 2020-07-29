package sde.ui

import org.w3c.dom.Element
import org.w3c.dom.events.MouseEvent
import pl.treksoft.kvision.core.Cursor
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.header
import pl.treksoft.kvision.html.Canvas
import pl.treksoft.kvision.html.button
import pl.treksoft.kvision.html.h3
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.toast.Toast
import sde.data.item.KeyframeItem
import sde.data.item.TimelineItem
import sde.utils.afterInsert
import kotlin.math.*

class Timeline(val timelineItem: TimelineItem) : Canvas(canvasWidth = 1000, canvasHeight = 60)
{
	private val possibleValueSteps = doubleArrayOf(10000.0, 5000.0, 1000.0, 500.0, 100.0, 50.0, 10.0, 5.0, 1.0, 0.5, 0.1, 0.05, 0.01, 0.005, 0.001, 0.0005, 0.0001)
	private val trackColours = arrayOf("forestgreen", "darkcyan", "darkviolet", "darkorange")

	private val actualWidth: Double
		get() = (canvasWidth ?: 0).toDouble()

	private val actualHeight: Double
		get() = (canvasHeight ?: 0).toDouble()

	private var inserted = false
	private var mouseOverItem: KeyframeItem? = null
	private var mouseX = 0.0
	private var mouseY = 0.0
	private var isMouseOver = false
	private var isPanning = false
	private var lastSelectedItem: KeyframeItem? = null
	private var resizeItem: KeyframeItem? = null
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
				isMouseOver = true
				redraw()
			}
			pointerleave = {
				isMouseOver = false
				mouseOverItem = null
				redraw()
			}
			pointermove = {
				val deltaX = it.offsetX - mouseX

				mouseX = it.offsetX
				mouseY = it.offsetY

				onMouseMove(deltaX, it.buttons, it.pointerId)
				redraw()
			}
			pointerdown = {
				onMouseDown(it)

				it.stopPropagation()
				it.preventDefault()
			}
			keydown = {
				if (it.key == "Control") {
					ctrlDown = true
				}
			}
			keyup = {
				if (it.key == "Control") {
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

		val sortedkeyframeItems = timelineItem.keyframes.sortedBy { it.time }.toList()

		drawIndicators(pixelsASecond)

		if (timelineItem.def.contentsMap.size == 1) {
			drawInterpolationPreview(sortedkeyframeItems, pixelsASecond)
		}

		drawKeyframeItems(sortedkeyframeItems, pixelsASecond)
		drawKeyframeInteractionButtons(sortedkeyframeItems, pixelsASecond)
	}

	private fun drawInterpolationPreview(keyframeItems: List<KeyframeItem>, pixelsASecond: Double) {
		if (keyframeItems.size == 0) {
			return
		}

		drawInterpolatedColours(keyframeItems, pixelsASecond)
		drawInterpolatedNumbers(keyframeItems, pixelsASecond)
	}

	private fun drawInterpolatedColours(keyframeItems: List<KeyframeItem>, pixelsASecond: Double) {
		val numColours = keyframeItems[0].colours.size
		val linePad = 2
		val lineHeight = 5.0
		val bottomPad = (actualHeight - (lineHeight * numColours + (numColours - 1) * linePad)) / 2
		for (i in 0 until numColours) {
			val drawPos = bottomPad + (lineHeight + linePad) * i

			for (ii in 0 until keyframeItems.size-1) {
				val thiskeyframeItem = keyframeItems[ii]
				val nextkeyframeItem = keyframeItems[ii+1]

				val thisCol = thiskeyframeItem.colours[i]
				val nextCol = nextkeyframeItem.colours[i]

				val thisDrawPos = thiskeyframeItem.time * pixelsASecond + timelineItem.leftPad
				val nextDrawPos = nextkeyframeItem.time * pixelsASecond + timelineItem.leftPad

				val gradient = context2D.createLinearGradient(0.0, 0.0, nextDrawPos - thisDrawPos, 0.0)
				gradient.addColorStop(0.0, "rgb(${thisCol.value})")
				gradient.addColorStop(1.0, "rgb(${nextCol.value})")

				context2D.fillStyle = gradient
				context2D.fillRect(thisDrawPos, drawPos, nextDrawPos - thisDrawPos, lineHeight)
			}

			for (ii in 0 until keyframeItems.size) {
				val thiskeyframeItem = keyframeItems[ii]

				val thisCol = thiskeyframeItem.colours[i]

				val thisDrawPos = thiskeyframeItem.time * pixelsASecond + timelineItem.leftPad

				context2D.fillStyle = "rgb(${thisCol.value})"
				context2D.fillRect(thisDrawPos-5, (drawPos+lineHeight/2) - 5, 10.0, 10.0)

				context2D.strokeStyle = borderDarkColour
				context2D.lineWidth = 1.0
				context2D.strokeRect(thisDrawPos-5, (drawPos+lineHeight/2) - 5, 10.0, 10.0)
			}
		}
	}

	private fun drawInterpolatedNumbers(keyframeItems: List<KeyframeItem>, pixelsASecond: Double) {
		val numNumbers = keyframeItems[0].numbers.size
		var min = Float.MAX_VALUE
		var max = -Float.MAX_VALUE

		for (keyframeItem in keyframeItems) {
			for (num in keyframeItem.numbers) {
				if (num.value < min) min = num.value
				if (num.value > max) max = num.value
			}
		}

		if (min == max) {
			max += 1.0f
		}

		for (i in 0 until numNumbers) {
			val col = trackColours[i]

			for (ii in 0 until keyframeItems.size-1) {
				val thiskeyframeItem = keyframeItems[ii]
				val nextkeyframeItem = keyframeItems[ii+1]

				val thisNum = thiskeyframeItem.numbers[i]
				val nextNum = nextkeyframeItem.numbers[i]

				val thisAlpha = (thisNum.value - min) / (max - min)
				val nextAlpha = (nextNum.value - min) / (max - min)

				val thisH = (actualHeight - 20) - (actualHeight - 25) * thisAlpha
				val nextH = (actualHeight - 20) - (actualHeight - 25) * nextAlpha

				val thisDrawPos = thiskeyframeItem.time * pixelsASecond + timelineItem.leftPad
				val nextDrawPos = nextkeyframeItem.time * pixelsASecond + timelineItem.leftPad

				context2D.strokeStyle = col
				context2D.lineWidth = 3.0
				context2D.beginPath()
				context2D.moveTo(thisDrawPos, thisH)
				context2D.lineTo(nextDrawPos, nextH)
				context2D.stroke()
			}

			for (ii in 0 until keyframeItems.size) {
				val thiskeyframeItem = keyframeItems[ii]

				val thisNum = thiskeyframeItem.numbers[i]

				val thisAlpha = (thisNum.value - min) / (max - min)

				val thisH = (actualHeight - 20) - (actualHeight - 25) * thisAlpha

				val thisDrawPos = thiskeyframeItem.time * pixelsASecond + timelineItem.leftPad

				context2D.fillStyle = col
				context2D.fillRect(thisDrawPos-5, thisH - 5, 10.0, 10.0)

				context2D.strokeStyle = borderDarkColour
				context2D.lineWidth = 1.0
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

			context2D.lineWidth = 1.0
			context2D.font = "10px Arial"

			val timeText = time.toString().split("0000")[0]
			val textSize = context2D.measureText(timeText)
			val fontHeight = 10

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
				context2D.lineWidth = 1.0
				context2D.beginPath()
				context2D.moveTo(mpos, 20.0)
				context2D.lineTo(mpos, actualHeight - 20)
				context2D.stroke()
			}
		}
	}

	private fun drawKeyframeItems(keyframeItems: List<KeyframeItem>, pixelsASecond: Double) {
		for (keyframeItem in keyframeItems) {
			var thickness = if (keyframeItem.isSelected) 2 else 1
			if (keyframeItem == mouseOverItem) thickness++

			val col = if (keyframeItem.isSelected) selectionBorderColour else borderLightColour

			val width = getKeyframeItemWidth(keyframeItem)

			val preview = keyframeItem.getImagePreview { redraw() }
			if (preview != null) {
				context2D.fillStyle = "rgb(${keyframeItem.def.background})"
				context2D.fillRect(keyframeItem.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)

				context2D.drawImage(preview, keyframeItem.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)

				context2D.strokeStyle = col
				context2D.lineWidth = thickness.toDouble()
				context2D.strokeRect(keyframeItem.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)
			} else {
				if (timelineItem.def.contentsMap.size > 1) {
					val name = keyframeItem.def.name

					context2D.lineWidth = 1.0
					context2D.font = "10px Arial"
					val textSize = context2D.measureText(name)
					val fontHeight = 10.0

					context2D.fillStyle = "rgb(${keyframeItem.def.background})"
					context2D.fillRect(keyframeItem.time * pixelsASecond + timelineItem.leftPad, fontHeight + 5, width, actualHeight - 15 - fontHeight)

					context2D.strokeStyle = col
					context2D.lineWidth = thickness.toDouble()
					context2D.strokeRect(keyframeItem.time * pixelsASecond + timelineItem.leftPad, fontHeight + 5, width, actualHeight - 15 - fontHeight)

					context2D.fillStyle = "white"
					context2D.fillText(name, max(0.0, (keyframeItem.time * pixelsASecond + timelineItem.leftPad + width / 2) - (textSize.width / 2.0)), fontHeight)
				} else {
					context2D.fillStyle = "rgb(${keyframeItem.def.background})"
					context2D.fillRect(keyframeItem.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)

					context2D.strokeStyle = col
					context2D.lineWidth = thickness.toDouble()
					context2D.strokeRect(keyframeItem.time * pixelsASecond + timelineItem.leftPad, 5.0, width, actualHeight - 20)
				}
			}
		}
	}

	private fun drawKeyframeInteractionButtons(keyframeItems: List<KeyframeItem>, pixelsASecond: Double) {
		if (!isMouseOver) return

		for (keyframe in keyframeItems) {
			if (keyframe.isSelected) {
				val x = keyframe.time * pixelsASecond + timelineItem.leftPad

				// edit
				context2D.fillStyle = backgroundNormalColour
				context2D.fillRect(x, actualHeight - 16, 14.0, 14.0)

				context2D.strokeStyle = borderNormalColour
				context2D.lineWidth = 1.0
				context2D.strokeRect(x, actualHeight - 16, 14.0, 14.0)

				context2D.font = "bold 12px Arial"
				context2D.fillStyle = "white"
				context2D.fillText("?", x+4, actualHeight - 5)

				// remove
				context2D.fillStyle = backgroundNormalColour
				context2D.fillRect(x, 2.0, 14.0, 14.0)

				context2D.strokeStyle = borderNormalColour
				context2D.lineWidth = 1.0
				context2D.strokeRect(x, 2.0, 14.0, 14.0)

				context2D.font = "bold 14px Arial"
				context2D.fillStyle = "red"
				context2D.fillText("-", x+4, 12.0)
			}
		}

		if (mouseOverItem == null) {
			// add
			context2D.fillStyle = backgroundNormalColour
			context2D.fillRect(mouseX - 7, 2.0, 14.0, 14.0)

			context2D.strokeStyle = borderNormalColour
			context2D.lineWidth = 1.0
			context2D.strokeRect(mouseX - 7, 2.0, 14.0, 14.0)

			context2D.font = "bold 14px Arial"
			context2D.fillStyle = "green"
			context2D.fillText("+", mouseX - 4, 15.0)
		}
	}

	private fun getKeyframeItemWidth(keyframeItem: KeyframeItem): Double {
		val pixelsASecond = actualWidth / timelineItem.timelineRange
		if (keyframeItem.duration > 0f) return keyframeItem.duration * pixelsASecond

		val preview = keyframeItem.getImagePreview { redraw() }

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

	private fun onMouseDown(mouseEvent: MouseEvent) {
		val pixelsASecond = actualWidth / timelineItem.timelineRange

		val clickPos = mouseX - timelineItem.leftPad
		val clickItem = getItemAt(clickPos)

		isPanning = false
		isResizing = false

		if (clickItem == null) {
			if (mouseY <= 15.0) {
				if (timelineItem.def.contentsMap.size == 1) {
					val def = timelineItem.def.contentsMap.values.first()
					val newItem = timelineItem.create(def) as KeyframeItem
					newItem.time = (clickPos / pixelsASecond).toFloat()
					timelineItem.children.sortBy { (it as? KeyframeItem)?.time ?: 0f }
				} else {
					val menu = Modal("Add keyframe") {
						vPanel {
							for (group in timelineItem.def.contents.sortedBy { it.first }) {
								h3(group.first)

								for (def in group.second.sortedBy { it.name }) {
									button(def.name) {
										onClick {
											this@Modal.hide()

											val newItem = timelineItem.create(def) as KeyframeItem
											newItem.time = (clickPos / pixelsASecond).toFloat()
											timelineItem.children.sortBy { (it as? KeyframeItem)?.time ?: 0f }
										}
									}
								}
							}
						}
					}
					menu.show()
				}
			} else {
				isPanning = true

				for (timeline in timelineItem.timelineGroup) {
					for (keyframeItem in timeline.keyframes) {
						keyframeItem.isSelected = false
					}
				}
			}
		} else {
			val withinBox = (mouseX - clickItem.time * pixelsASecond) <= 20
			if (withinBox && mouseY <= 15.0 && clickItem.isSelected) {
				clickItem.removeFromCollection()
			} else if (withinBox && mouseY >= actualHeight-16 && clickItem.isSelected) {
				val modal = Modal("Edit ${clickItem.name}") {
					val editor = DataItemEditor(timelineItem.document.scope!!)
					clickItem.isExpanded = true
					editor.rootItems.add(clickItem)
					editor.update()
					add(editor)
				}
				modal.show()
			} else {
				clickItem.isSelected = true
				lastSelectedItem = clickItem

				resizeItem = clickItem
				startPos = clickPos

				if (!clickItem.isDurationLocked) {
					val time = clickItem.time * pixelsASecond
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
					var timelineIndex = 0
					for (timeline in timelineGroup) {
						for (keyframeItem in timeline.keyframes) {
							if (keyframeItem.isSelected) {
								val startOffset = (clickPos / pixelsASecond) - keyframeItem.time
								val dragAction = DragAction(keyframeItem, keyframeItem.time.toDouble(), startOffset, timelineIndex)
								draggedActions.add(dragAction)

								if (keyframeItem == clickItem) {
									draggedAction = dragAction
								}
							}
						}
						timelineIndex++
					}

					isDraggingItems = true
					cursor = Cursor.MOVE
				}

				generateSnapList(clickItem)
			}
		}

		redraw()
	}

	private fun getItemAt(clickPos: Double): KeyframeItem? {
		val pixelsASecond = actualWidth / timelineItem.timelineRange

		for (keyframeItem in timelineItem.keyframes) {
			val time = keyframeItem.time * pixelsASecond
			val diff = clickPos - time

			if (diff >= 0 && diff < getKeyframeItemWidth(keyframeItem)) {
				return keyframeItem
			}
		}

		return null
	}

	private fun generateSnapList(dragged: KeyframeItem) {
		snapLines.clear()

		for (timeline in timelineItem.timelineGroup) {
			for (keyframeItem in timeline.keyframes) {
				if (keyframeItem == dragged || keyframeItem.isSelected) continue

				val time = keyframeItem.time.toDouble()
				if (!snapLines.contains(time)) snapLines.add(time)
				if (keyframeItem.duration > 0f) {
					val time = keyframeItem.endTime.toDouble()
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
				onDragKeyframeItems(clickPos, pixelsASecond)
			} else if (isPanning) {
				onPan(deltaX, pointerId)
			} else if (isResizing) {
				onResizeKeyframeItems(clickPos, pixelsASecond, pointerId)
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
	}

	private fun onDragKeyframeItems(clickPos: Double, pixelsASecond: Double) {
		val dragItems = draggedActions
		val dragItem = draggedAction!!

		// do time change
		val newTime = clickPos / pixelsASecond - dragItem.actionStartOffset
		var roundedTime = snap(newTime)

		if (dragItem.keyframeItem.duration > 0f && !ctrlDown) {
			val endTime = newTime + dragItem.keyframeItem.duration
			val snapped = snap(endTime)
			roundedTime += snapped - endTime
		}

		val diff = roundedTime - dragItem.originalPosition

		for (item in dragItems) {
			item.keyframeItem.time = (item.originalPosition + diff).toFloat()
		}

		// do timeline change
		val timelineGroup = timelineItem.timelineGroup.toList()
		val currentTimelineIndex = timelineGroup.indexOf(timelineItem)
		if (currentTimelineIndex != timelineGroup.indexOf(dragItem.keyframeItem.timelineItem)) {
			val idealChange = currentTimelineIndex - dragItem.startTimelineIndex

			// move items
			for (item in dragItems) {
				val itemTimelineIndex = item.startTimelineIndex

				for (i in 0 until idealChange.absoluteValue+1) {
					val signedI = idealChange.sign * i
					val index = itemTimelineIndex + (idealChange - signedI)
					val targetTimeline = timelineGroup[index]

					if (targetTimeline.def.contentsMap.values.contains(item.keyframeItem.def)) {
						item.keyframeItem.removeFromCollection()
						targetTimeline.children.add(item.keyframeItem)
					}
				}
			}
		}

		cursor = Cursor.MOVE
	}

	private fun onResizeKeyframeItems(clickPos: Double, pixelsASecond: Double, pointerId: Int) {
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

	private fun onPan(deltaX: Double, pointerId: Int) {
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
	}

	private fun endDrag() {
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
				timeline.children.sortBy { (it as? KeyframeItem)?.time ?: 0f }
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

class DragAction(var keyframeItem: KeyframeItem, val originalPosition: Double, val actionStartOffset: Double, val startTimelineIndex: Int)