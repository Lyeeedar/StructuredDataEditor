package sde.ui

import io.kvision.core.Container
import io.kvision.html.Span
import sde.utils.afterInsert
import sde.utils.disableSelection

class TextBlock(text: String = "", rich: Boolean = false, init: (TextBlock.() -> Unit)? = null): Span(text, rich = rich)
{
    init {
        afterInsert {
            it.disableSelection()
        }

        init?.invoke(this)
    }
}

fun Container.textBlock(text: String = "", rich: Boolean = false, init: (TextBlock.() -> Unit)? = null): TextBlock
{
    val textBlock = TextBlock(text, rich).apply { init?.invoke(this) }
    this.add(textBlock)
    return textBlock
}