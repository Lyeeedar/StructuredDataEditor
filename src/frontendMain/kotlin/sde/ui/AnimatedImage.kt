package sde.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.html.Div
import io.kvision.html.Image
import sde.utils.*

class AnimatedImage(val loadScope: CoroutineScope, val size: Int = 24, val imagePaths: List<String>) : Image("")
{
	val images = ArrayList<String>()
	val updateJob: Job

	init
	{
		visible = false
		width = CssSize(size, UNIT.px)
		height = CssSize(size, UNIT.px)

		afterInsert {
			val el = it
			it.hover({
				         el.css("transform", "scale(10)")
			         }, {
				         el.css("transform", "scale(1)")
			         })
			el.css("image-rendering", "crisp-edges")
		}

		val loadJob = loadScope.launch {
			for (src in imagePaths)
			{
				val img = ImageCache.getImageUrl(src)
				images.add(img)
			}
		}
		updateJob = loadScope.launch {
			loadJob.join()
			if (images.size > 0) {
				visible = true

				if (images.size > 1)
				{
					var i = 0
					while (true)
					{
						val img = images[i]

						src = img

						i++
						if (i >= images.size)
						{
							i = 0
						}

						delay(500)
					}
				} else {
					src = images[0]
				}
			}
		}

		afterInsert {
			it.disableSelection()
		}
	}

	override fun dispose() {
		super.dispose()
		updateJob.cancel()
	}
}