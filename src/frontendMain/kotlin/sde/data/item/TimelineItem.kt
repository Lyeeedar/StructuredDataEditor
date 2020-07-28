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
	val keyframes: Sequence<KeyframeItem>
		get() {
			return sequence {
				for (child in children) {
					if (child is KeyframeItem) {
						yield(child!!)
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

	var leftPad: Double = 10.0

	var timeline: Timeline = Timeline(this)
	init {
	    registerListener("childEvent") {
		    if (isVisible())
		    {
			    timeline.redraw()
		    }
		}
		children.onUpdate.add {
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