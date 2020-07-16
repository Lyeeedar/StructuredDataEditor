package sde.ui

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.html.image
import sde.utils.hover

class ImageButton(src: String?, init: (ImageButton.() -> Unit)? = null) : Div()
{
	init
	{
		border = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.LIGHTGRAY))
		background = Background(Color.name(Col.DARKGRAY))

		image(src)

		hover(
			{
				background = Background(Color.name(Col.GREEN))
			},
			{
				background = Background(Color.name(Col.DARKGRAY))
			})

		@Suppress("LeakingThis")
		init?.invoke(this)
	}
}

fun Container.imageButton(src: String?, init: (ImageButton.() -> Unit)? = null): ImageButton
{
	val imageButton = ImageButton(src).apply { init?.invoke(this) }
	this.add(imageButton)
	return imageButton
}