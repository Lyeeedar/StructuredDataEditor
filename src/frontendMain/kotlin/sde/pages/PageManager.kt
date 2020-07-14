package sde.pages

import pl.treksoft.kvision.core.onEvent
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
			tabContainer.addTab(page.name, page.createComponent(), closable = page.closeable)
		}

		val el = tabContainer.getElement()
		if (!addedListeners && el != null)
		{
			addedListeners = true

			el.addEventListener("tabClosing", {
				val index = it.asDynamic().detail.data as Int
				val page = pages[index]

				if (!page.canClose()) {
					it.preventDefault()
				}
			})

			el.addEventListener("tabClosed", {
				val index = it.asDynamic().detail.data as Int
				val page = pages[index]

				pages.remove(page)
				page.close()
			})
		}
	}

	var addedListeners = false
}