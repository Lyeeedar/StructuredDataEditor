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
		updateBackground()

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
		opacity = if (disabled) 0.6 else 1.0
		background = when
		{
			isMouseHover && !disabled -> Background(mouseOverBackgroundColour)
			else -> Background(backgroundLightColour)
		}
		border = when
		{
			isMouseHover && !disabled -> Border(CssSize(1, UNIT.px), BorderStyle.SOLID, mouseOverBorderColour)
			else -> Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderLightColour)
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