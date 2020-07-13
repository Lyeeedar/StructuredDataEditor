package sde.pages

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.html.button
import pl.treksoft.kvision.html.h1
import sde.Model

class StartPage(pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "Start Page"

	override fun createComponent(): Component
	{
		return Div {
			h1("Structured Data Editor")
			button("Open project") {
				onClick {
					GlobalScope.launch {
						val project = Model.getProject()
						val projectPage = ProjectPage(project, pageManager)
						pageManager.addPage(projectPage)
					}
				}
			}
		}
	}
}