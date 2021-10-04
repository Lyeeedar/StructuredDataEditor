package sde.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.kvision.core.Container
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.html.Div
import io.kvision.html.image
import sde.utils.afterInsert
import sde.utils.disableSelection
import sde.utils.imageFromFile

class AsyncLoadImage(val src: String, val fallback: String, val loadScope: CoroutineScope, val size: Int = 16, init: (AsyncLoadImage.() -> Unit)? = null) : Div()
{
    init {
	    val size = this.size
        image(fallback) {
            width = CssSize(size, UNIT.px)
            height = CssSize(size, UNIT.px)
        }

        afterInsert {
            it.disableSelection()
        }

        loadScope.launch {
            val newImage = imageFromFile(src) {
                width = CssSize(size, UNIT.px)
                height = CssSize(size, UNIT.px)
            }
            removeAll()
            add(newImage)
        }

        @Suppress("LeakingThis")
        init?.invoke(this)
    }
}

fun Container.asyncLoadImage(src: String, fallback: String, loadScope: CoroutineScope, size: Int = 16, init: (AsyncLoadImage.() -> Unit)? = null): AsyncLoadImage
{
    val asyncLoadImage = AsyncLoadImage(src, fallback, loadScope, size).apply { init?.invoke(this) }
    this.add(asyncLoadImage)
    return asyncLoadImage
}