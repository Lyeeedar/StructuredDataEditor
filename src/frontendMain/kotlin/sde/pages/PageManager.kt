package sde.pages

import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.panel.TabPanel

class PageManager
{
	val pages = ArrayList<AbstractPage>()
	lateinit var tabContainer: TabPanel

	fun loadPages() {
		pages.add(StartPage(this))
	}

	fun addPage(page: AbstractPage) {
		pages.add(page)
		updatePages()
	}

	fun fillRoot(root: Root) {
		root.removeAll()

		tabContainer = TabPanel()
		updatePages()
		root.add(tabContainer)
	}

	private fun updatePages() {
		tabContainer.removeAll()
		var index = 0
		for (page in pages) {
			page.pageTabIndex = index++
			tabContainer.addTab(page.name, page.createComponent())
		}
	}
}