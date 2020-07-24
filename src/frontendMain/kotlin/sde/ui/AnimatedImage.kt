package sde.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Image
import sde.utils.afterInsert
import sde.utils.disableSelection
import sde.utils.imageFromFile

class AnimatedImage(val loadScope: CoroutineScope, val size: Int = 16, val imagePaths: List<String>) : Div()
{
	val images = ArrayList<Image>()
	val updateJob: Job

	init
	{
		val loadJob = loadScope.launch {
			for (src in imagePaths)
			{
				val img = imageFromFile(src) {
					width = CssSize(size, UNIT.px)
					height = CssSize(size, UNIT.px)
				}
				images.add(img)
			}
		}
		updateJob = loadScope.launch {
			loadJob.join()

			if (images.size > 1)
			{
				var i = 0
				while (true)
				{
					val img = images[i]

					removeAll()
					add(img)

					i++
					if (i >= images.size)
					{
						i = 0
					}

					delay(500)
				}
			} else {
				removeAll()
				add(images[0])
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