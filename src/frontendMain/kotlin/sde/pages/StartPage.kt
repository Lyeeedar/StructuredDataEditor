package sde.pages

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Span

class StartPage : AbstractPage()
{
	override val name: String
		get() = "Start Page"

	override fun createComponent(): Component
	{
		return Span("This is the start page")
	}
}