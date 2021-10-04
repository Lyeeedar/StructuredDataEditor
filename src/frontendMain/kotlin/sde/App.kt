package sde

import kotlinx.coroutines.*
import org.w3c.dom.HTMLElement
import io.kvision.Application
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.i18n.DefaultI18nManager
import io.kvision.i18n.I18n
import io.kvision.panel.Root
import io.kvision.panel.root
import io.kvision.startApplication
import sde.pages.PageManager
import kotlinx.browser.document

class App : Application() {

	lateinit var pageManager: PageManager

	init
	{
		io.kvision.require("css/bootstrap.css")
	}

    override fun start(state: Map<String, Any>) {
        I18n.manager =
            DefaultI18nManager(
                mapOf(
                    "en" to io.kvision.require("i18n/messages-en.json"),
                    "pl" to io.kvision.require("i18n/messages-pl.json")
                )
            )
        val root = root("kvapp", addRow = false) {
			width = CssSize(100, UNIT.perc)
			height = CssSize(100, UNIT.perc)
        }
	    createPages(root)
    }

	fun createPages(root: Root) {
		pageManager = PageManager()
		pageManager.loadPages()
		pageManager.fillRoot(root)
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