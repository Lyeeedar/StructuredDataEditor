package sde.ui

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.html.image
import sde.utils.hover

class ImageButton(src: String?, init: (ImageButton.() -> Unit)? = null) : Div()
{
	var isMouseHover = false
	var disabled = false

	init
	{
		border = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, Color.name(Col.LIGHTGRAY))
		background = Background(Color.name(Col.DARKGRAY))

		image(src)

		hover(
			{
				isMouseHover = true
				updateBackground()
			},
			{
				isMouseHover = false
				updateBackground()
			})

		@Suppress("LeakingThis")
		init?.invoke(this)
	}

	fun updateBackground()
	{
		background = when
		{
			disabled -> Background(Color.name(Col.BLACK))
			isMouseHover -> Background(Color.name(Col.GREEN))
			else -> Background(Color.name(Col.DARKGRAY))
		}
	}

	fun setDisabled(value: Boolean)
	{
		disabled = value
		updateBackground()
	}
}

fun Container.imageButton(src: String?, init: (ImageButton.() -> Unit)? = null): ImageButton
{
	val imageButton = ImageButton(src).apply { init?.invoke(this) }
	this.add(imageButton)
	return imageButton
}