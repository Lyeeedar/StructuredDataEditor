package sde.ui

import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.html.Span
import sde.utils.afterInsert
import sde.utils.disableSelection

class TextBlock(text: String = "", init: (TextBlock.() -> Unit)? = null): Span(text)
{
    init {
        afterInsert {
            it.disableSelection()
        }

        init?.invoke(this)
    }
}

fun Container.textBlock(text: String = "", init: (TextBlock.() -> Unit)? = null): TextBlock
{
    val textBlock = TextBlock(text).apply { init?.invoke(this) }
    this.add(textBlock)
    return textBlock
}