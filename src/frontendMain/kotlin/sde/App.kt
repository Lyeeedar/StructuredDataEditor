package sde

import kotlinx.coroutines.*
import pl.treksoft.kvision.Application
import pl.treksoft.kvision.i18n.DefaultI18nManager
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.progress.progressBar
import pl.treksoft.kvision.startApplication
import sde.pages.PageManager

class App : Application() {

	lateinit var pageManager: PageManager

	init
	{
		pl.treksoft.kvision.require("css/bootstrap.css")
	}

    override fun start(state: Map<String, Any>) {
        I18n.manager =
            DefaultI18nManager(
                mapOf(
                    "en" to pl.treksoft.kvision.require("i18n/messages-en.json"),
                    "pl" to pl.treksoft.kvision.require("i18n/messages-pl.json")
                )
            )
        val root = root("kvapp") {
	        progressBar(5, 0, 10, striped = true, animated = true)
        }
	    createPages(root)
    }

	fun createPages(root: Root) {
		GlobalScope.launch {
			pageManager = PageManager()
			pageManager.loadPages()
			pageManager.fillRoot(root)
		}
	}

	override fun dispose(): Map<String, Any>
	{
		AppModel.appScope.cancel()
		return super.dispose()
	}
}

fun main() {
    startApplication(::App)
}

object AppModel
{
	val appScope = MainScope()
}