package sde.data

import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.bold
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.hPanel
import sde.pages.AbstractPage
import sde.pages.PageManager
import sde.ui.asyncLoadImage
import sde.utils.afterInsert
import sde.utils.disableSelection

class DataDocumentPage(val data: DataDocument, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = data.name

	override val closeable: Boolean
		get() = true

	init
	{
		data.scope = getPageScope()
	}

	override fun createTabHeader(): Component {
		return VPanel {
			bold(data.name.split("\\.")[0])
			hPanel {
				val def = data.root.def

				val imagePath = data.project.projectRootFolder + "/" + def.fileIcon
				val fallback = pl.treksoft.kvision.require("images/File.png") as String
				asyncLoadImage(imagePath, fallback, getPageScope())

				span(data.root.def.name) {
					color = Color("rgb(${def.fileColour})")
				}
			}

			afterInsert {
				it.disableSelection()
			}
		}
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