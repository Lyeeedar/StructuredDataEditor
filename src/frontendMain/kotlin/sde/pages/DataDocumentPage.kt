package sde.pages

import pl.treksoft.kvision.core.Component
import sde.data.DataDocument

class DataDocumentPage(val data: DataDocument, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = data.name

	override val closeable: Boolean
		get() = true

	override fun createComponent(): Component
	{
		return data.getComponent()
	}

	override fun canClose(): Boolean
	{
		return true
	}
}