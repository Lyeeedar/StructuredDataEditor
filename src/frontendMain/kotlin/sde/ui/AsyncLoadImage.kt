package sde.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.image
import sde.utils.afterInsert
import sde.utils.disableSelection
import sde.utils.imageFromFile

class AsyncLoadImage(val src: String, val fallback: String, val loadScope: CoroutineScope, val size: Int = 16, init: (AsyncLoadImage.() -> Unit)? = null) : Div()
{
    init {
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