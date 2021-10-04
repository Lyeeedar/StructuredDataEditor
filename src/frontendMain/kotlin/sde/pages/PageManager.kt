package sde.pages

import io.kvision.core.onEvent
import io.kvision.html.Div
import io.kvision.panel.Root
import io.kvision.panel.TabPanel
import sde.ui.TabControl

class PageManager
{
	val pages = ArrayList<AbstractPage>()
	lateinit var tabContainer: TabControl

	fun loadPages() {
		pages.add(StartPage(this))
	}

	fun addPage(page: AbstractPage) {
		pages.add(page)
		updatePages()
	}

	fun fillRoot(root: Root) {
		root.removeAll()

		tabContainer = TabControl()
		updatePages()
		root.add(tabContainer)
	}

	private fun updatePages() {
		tabContainer.removeAllTabs()

		for (page in pages) {
			tabContainer.addTab(page.createTabHeader(), page.createComponent(), page, page.closeable, { page.canClose() }, { pages.remove(page); page.close() })
		}
	}
}