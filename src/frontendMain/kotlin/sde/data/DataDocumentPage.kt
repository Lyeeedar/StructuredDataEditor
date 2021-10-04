package sde.data

import io.kvision.core.Color
import io.kvision.core.Component
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.html.b
import io.kvision.html.span
import io.kvision.panel.GridPanel
import io.kvision.panel.HPanel
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
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
			b(data.name.split("\\.")[0])
			hPanel {
				val def = data.root.def

				val imagePath = data.project.projectRootFolder + "/" + def.fileIcon
				val fallback = io.kvision.require("images/File.png") as String
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
		return GridPanel(templateRows = "auto 1fr") {
			id = "DataDocumentPage"

			width = CssSize(100, UNIT.perc)
			height = CssSize(100, UNIT.perc)

			add(HPanel {
				add(data.undoRedoManager.undoButton)
				add(data.undoRedoManager.redoButton)
			}, 1, 1)
			add(data.getComponent(), 1, 2)
		}
	}

	override fun canClose(): Boolean
	{
		return true
	}
}