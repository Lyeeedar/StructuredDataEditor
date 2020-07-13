package sde.pages

import pl.treksoft.kvision.core.Component

abstract class AbstractPage(val pageManager: PageManager)
{
	abstract val name: String
	abstract fun createComponent(): Component
}