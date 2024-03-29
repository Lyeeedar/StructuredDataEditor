package sde.pages

import kotlinx.coroutines.*
import io.kvision.core.Component
import io.kvision.toast.Toast
import sde.AppModel
import sde.utils.prepareStackForToast

abstract class AbstractPage(val pageManager: PageManager)
{
	private val scope = MainScope() + CoroutineName((this::class.simpleName ?: "") + id++)
	fun getPageScope() = scope
	fun launch(jobBody: suspend ()->Unit): Job {
		return scope.launch {
			try {
				jobBody()
			} catch (ex: Throwable) {
				Toast.error(ex.message + "<br />" + ex.asDynamic().stack.toString().prepareStackForToast(), "Unhandled exception")
			}
		}
	}

	abstract val name: String
	abstract val closeable: Boolean
	abstract fun createComponent(): Component
	abstract fun createTabHeader(): Component

	fun show() {
		pageManager.tabContainer.selectTab(this)
	}

	abstract fun canClose(): Boolean

	open fun close() {
		scope.cancel("Page disposed")
	}

	companion object
	{
		private var id = 0
	}
}