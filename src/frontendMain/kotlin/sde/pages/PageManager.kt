package sde.pages

import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.panel.TabPanel

class PageManager
{
	val pages = ArrayList<AbstractPage>()

	fun loadPages() {
		pages.add(StartPage())
	}

	fun fillRoot(root: Root) {
		root.removeAll()

		val tabContainer = TabPanel()
		for (page in pages) {
			tabContainer.addTab(page.name, page.createComponent())
		}
		root.add(Div() {
			add(tabContainer)
		})
	}
}