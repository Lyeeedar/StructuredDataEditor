package sde.data.item

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasImageSource
import org.w3c.dom.Image
import org.w3c.dom.ImageBitmapOptions
import sde.data.DataDocument
import sde.data.definition.KeyframeDefinition
import sde.utils.ImageCache
import kotlin.browser.window

class KeyframeItem(definition: KeyframeDefinition, document: DataDocument) : AbstractStructItem<KeyframeDefinition>(definition, document) {
	val timeItem by lazy { children.firstOrNull { it.def.name == "Time" } as? NumberItem ?: throw Exception("Unable to find a Time child on ${def.name}") }
	val durationItem by lazy { children.firstOrNull { it.def.name == "Duration" } as? NumberItem }

	val timelineItem: TimelineItem
		get() = parent as TimelineItem

	var time: Float
		get() = timeItem.value
		set(value) {
			var value = value
			if (value < timeItem.def.minValue) {
				value = timeItem.def.minValue
			}
			if (value > timeItem.def.maxValue) {
				value = timeItem.def.maxValue
			}

			timeItem.value = value
		}

	var duration: Float
		get() = durationItem?.value ?: 0f
		set(value) {
			val durationItem = durationItem
			if (durationItem != null) {
				var value = value
				if (value < durationItem.def.minValue) {
					value = durationItem.def.minValue
				}
				if (value > durationItem.def.maxValue) {
					value = durationItem.def.maxValue
				}

				durationItem.value = value
			}
		}

	val endTime: Float
		get() = time + duration

	val colours: List<ColourItem>
		get() = children.filterIsInstance<ColourItem>()

	val numbers: List<NumberItem>
		get() = children.filterIsInstance<NumberItem>().filter { it != timeItem && it != durationItem }

	val file: FileItem?
		get() = children.firstOrNull { it is FileItem } as? FileItem

	val isDurationLocked: Boolean
		get() {
			if (durationItem == null) return true
			return false
		}

	var isSelected: Boolean = false
		set(value) {
			field = value
			timelineItem.timeline.redraw()
		}

	var cachedImage: Image? = null
	var cachedImagePath: String? = null
	fun getImagePreview(completionFunc: ()->Unit): CanvasImageSource? {
		val file = file ?: return null

		document.scope?.launch {
			val fullPath = file.getFullPath()

			if (cachedImagePath != fullPath) {
				cachedImage = null
				cachedImagePath = ""

				if (fullPath.endsWith(".png")) {
					val url = ImageCache.getImageUrl(file.getFullPath())
					val image = Image(40, 40)
					image.src = url
					image.onload = {
						completionFunc.invoke()
					}
					cachedImage = image
					cachedImagePath = fullPath
				}
			}
		}

		return cachedImage
	}

    override fun createContents() {
        def.createContents(this, document)
    }
}