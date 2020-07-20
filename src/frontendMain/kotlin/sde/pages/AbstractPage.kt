package sde.pages

import kotlinx.coroutines.*
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.toast.Toast

abstract class AbstractPage(val pageManager: PageManager)
{
	private val scope = MainScope() + CoroutineName(this::class.simpleName ?: "")
	fun getPageScope() = scope
	fun launch(jobBody: suspend ()->Unit): Job {
		return scope.launch {
			try {
				jobBody()
			} catch (ex: Throwable) {
				Toast.error(ex.message + "\n" + ex.asDynamic().stack, "Unhandled exception")
			}
		}
	}

	abstract val name: String
	abstract val closeable: Boolean
	abstract fun createComponent(): Component

	fun show() {
		pageManager.tabContainer.selectTab(this)
	}

	abstract fun canClose(): Boolean

	open fun close() {
		scope.cancel("Page disposed")
	}
}