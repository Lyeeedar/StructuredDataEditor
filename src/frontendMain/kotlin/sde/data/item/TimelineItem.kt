package sde.data.item

import sde.data.DataDocument
import sde.data.definition.TimelineDefinition

class TimelineItem(definition: TimelineDefinition, document: DataDocument) : AbstractCollectionItem<TimelineDefinition>(definition, document)
{
	private val keyframeMap = HashMap<StructItem, Keyframe>()

	val keyframes: Sequence<Keyframe>
		get() {
			return sequence {
				for (child in children) {
					if (child is StructItem) {

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
		get() = keyframes.map { it.time.def.maxValue }.max() ?: 1f

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

	var leftPad: Int = 0
}

class Keyframe(val item: StructItem)
{
	val time = item.children.firstOrNull { it.def.name == "Time" } as? NumberItem ?: throw Exception("Unable to find a Time child on ${item.def.name}")
	val duration = item.children.firstOrNull { it.def.name == "Duration" } as? NumberItem

	val endTime: Float
		get() = time.value + (duration?.value ?: 0f)

	val colours: List<ColourItem>
		get() = item.children.filterIsInstance<ColourItem>()

	val numbers: List<NumberItem>
		get() = item.children.filterIsInstance<NumberItem>()
}