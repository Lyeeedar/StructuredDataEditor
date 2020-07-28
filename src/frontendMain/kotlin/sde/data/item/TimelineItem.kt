package sde.data.item

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.*
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.html.Div
import sde.data.DataDocument
import sde.data.definition.KeyframeDefinition
import sde.data.definition.TimelineDefinition
import sde.ui.Timeline
import sde.utils.ImageCache
import sde.utils.afterInsert
import kotlin.browser.window

class TimelineItem(definition: TimelineDefinition, document: DataDocument) : AbstractCollectionItem<TimelineDefinition>(definition, document)
{
	private val keyframeMap = HashMap<KeyframeItem, Keyframe>()

	val keyframes: Sequence<Keyframe>
		get() {
			return sequence {
				for (child in children) {
					if (child is KeyframeItem) {

						var item = keyframeMap[child]
						if (item == null) {
							item = Keyframe(child)
							keyframeMap[child] = item
						}

						yield(item!!)
					}
				}
			}
		}

	val maxTime: Float
		get() = keyframes.map { it.timeItem.def.maxValue }.max() ?: 1f

	var timelineRange: Float = -1f
		get()
		{
			if (field == -1f) {
				var max = maxTime
				if (keyframes.count() > 0) {
					max = keyframes.last().endTime
				}

				if (max == 0f) {
					max = maxTime
				}
				if (max == Float.MAX_VALUE) {
					max = 1f
				}

				field = max * 1.1f
			}

			return field
		}

	val timelineGroup: Sequence<TimelineItem>
		get() {
			val parent = parent ?: return emptySequence()

			val thisIndex = parent.children.indexOf(this)
			if (thisIndex == -1) return emptySequence()

			var minIndex = thisIndex
			var maxIndex = thisIndex

			// read back to first
			for (i in thisIndex downTo 0) {
				val item = parent.children[i]

				if (item is TimelineItem) {
					minIndex = i
				} else {
					break
				}
			}

			// read forward to end
			for (i in thisIndex until parent.children.size) {
				val item = parent.children[i]

				if (item is TimelineItem) {
					maxIndex = i
				} else {
					break
				}
			}

			return sequence {
				for (i in minIndex until maxIndex+1) {
					val item = parent.children[i] as? TimelineItem ?: continue
					yield(item)
				}
			}
		}

	var leftPad: Int = 10

	var timeline: Timeline = Timeline(this)
	init {
	    registerListener("childEvent") {

		    if (isVisible())
		    {
			    timeline.redraw()
		    }
		}
	}

	override fun getEditorComponent(): Component {
		return Div {
			height = CssSize(50, UNIT.px)
			width = CssSize(100, UNIT.perc)

			document.scope?.launch {
				while (true) {
					val el = getElement() as? HTMLElement ?: continue

					if (timeline.canvasWidth != el.offsetWidth) {
						timeline.canvasWidth = el.offsetWidth
					}

					add(timeline)
					timeline.redraw()

					break
				}
			}

			afterInsert {

			}
		}
	}
}

class Keyframe(val item: KeyframeItem)
{
	val timeItem = item.children.firstOrNull { it.def.name == "Time" } as? NumberItem ?: throw Exception("Unable to find a Time child on ${item.def.name}")
	val durationItem = item.children.firstOrNull { it.def.name == "Duration" } as? NumberItem

	var time: Float
		get() = timeItem.value
		set(value) {
			timeItem.value = value
		}

	var duration: Float
		get() = durationItem?.value ?: 0f
		set(value) {
			durationItem?.value = value
		}

	val endTime: Float
		get() = time + duration

	val colours: List<ColourItem>
		get() = item.children.filterIsInstance<ColourItem>()

	val numbers: List<NumberItem>
		get() = item.children.filterIsInstance<NumberItem>()

	val file: FileItem?
		get() = item.children.firstOrNull { it is FileItem } as? FileItem

	var isSelected = false

	var cachedImage: CanvasImageSource? = null
	var cachedImagePath: String? = null
	fun getImagePreview(completionFunc: ()->Unit): CanvasImageSource? {
		val file = file ?: return null

		GlobalScope.launch {
			val fullPath = file.getFullPath()

			if (cachedImagePath != fullPath) {
				cachedImage = null

				if (fullPath.endsWith(".png")) {
					val blob = ImageCache.getImageBlob(file.getFullPath())
					cachedImage = window.createImageBitmap(blob, ImageBitmapOptions()).await()
					cachedImagePath = fullPath

					completionFunc.invoke()
				}
			}
		}

		return cachedImage
	}
}