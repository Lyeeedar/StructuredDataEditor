package sde.pages

import kotlinx.coroutines.*
import pl.treksoft.kvision.core.Component

abstract class AbstractPage(val pageManager: PageManager)
{
	val scope = MainScope() + CoroutineName(this::class.simpleName ?: "")
	var pageTabIndex: Int = 0

	abstract val name: String
	abstract val closeable: Boolean
	abstract fun createComponent(): Component

	fun show() {
		pageManager.tabContainer.activeIndex = pageTabIndex
	}

	abstract fun canClose(): Boolean

	open fun close() {
		scope.cancel("Page disposed")
	}
}