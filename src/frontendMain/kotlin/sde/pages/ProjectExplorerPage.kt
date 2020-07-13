package sde.pages

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Span
import sde.project.Project

class ProjectExplorerPage(val project: Project, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "Project Explorer"

	override fun createComponent(): Component
	{
		return Span("Im the project!")
	}
}