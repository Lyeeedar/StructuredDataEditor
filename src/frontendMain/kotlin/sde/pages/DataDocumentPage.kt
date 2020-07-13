package sde.pages

import pl.treksoft.kvision.core.Component
import sde.data.DataDocument

class DataDocumentPage(val data: DataDocument, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = data.name

	override fun createComponent(): Component
	{
		TODO("Not yet implemented")
	}
}