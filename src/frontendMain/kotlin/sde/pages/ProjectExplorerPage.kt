package sde.pages

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Span
import sde.project.Project

class ProjectExplorerPage(val project: Project, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "${project.name} Project"

	override val closeable: Boolean
		get() = true

	override fun createComponent(): Component
	{
		return Span("Im the project!")
	}

	override fun canClose(): Boolean
	{
		for (page in pageManager.pages) {
			if (page != this && !page.canClose()) {
				return false
			}
		}

		return true
	}

	override fun close()
	{
		super.close()

		for (page in pageManager.pages) {
			if (page != this) {
				page.close()
			}
		}

		pageManager.pages.clear()
		pageManager.addPage(StartPage(pageManager))
	}
}