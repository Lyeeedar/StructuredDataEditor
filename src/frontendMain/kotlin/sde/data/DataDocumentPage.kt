package sde.data

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.hPanel
import sde.pages.AbstractPage
import sde.pages.PageManager

class DataDocumentPage(val data: DataDocument, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = data.name

	override val closeable: Boolean
		get() = true

	init
	{
		data.startChangeWatcher(getPageScope())
	}

	override fun createComponent(): Component
	{
		return Div {
			hPanel {
				add(data.undoRedoManager.undoButton)
				add(data.undoRedoManager.redoButton)
			}
			add(data.getComponent())
		}
	}

	override fun canClose(): Boolean
	{
		return true
	}
}