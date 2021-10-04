package sde.ui

import io.kvision.core.*
import io.kvision.html.Div
import io.kvision.html.image
import sde.utils.afterInsert
import sde.utils.disableSelection

class ImageButton(src: String?, init: (ImageButton.() -> Unit)? = null) : Div()
{
	var backgroundCol = backgroundLightColour
	var borderCol = borderLightColour

	var isMouseHover = false
	var disabled = false

	init
	{
		image(src)

		afterInsert {
			it.disableSelection()
			it.hover(
					{
						isMouseHover = true
						updateBackground()
					},
					{
						isMouseHover = false
						updateBackground()
					})
		}

		@Suppress("LeakingThis")
		init?.invoke(this)

		updateBackground()
	}

	fun updateBackground()
	{
		opacity = if (disabled) 0.6 else 1.0
		background = when
		{
			isMouseHover && !disabled -> Background(mouseOverBackgroundColour)
			else -> Background(backgroundCol)
		}
		border = when
		{
			isMouseHover && !disabled -> Border(CssSize(1, UNIT.px), BorderStyle.SOLID, mouseOverBorderColour)
			else -> Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderCol)
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