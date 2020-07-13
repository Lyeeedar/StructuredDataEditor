package sde.pages

import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.onClick
import pl.treksoft.kvision.form.upload.uploadInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.panel.vPanel
import sde.Services
import sde.project.Project

class StartPage(pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "Start Page"

	override val closeable: Boolean
		get() = false

	lateinit var recentProjectsDiv: Div

	override fun createComponent(): Component
	{
		return Div {
			h1("Structured Data Editor")

			// boxes
			hPanel(wrap = FlexWrap.WRAP) {
				div {
					addCssClass("jumbotron")
					padding = CssSize(1, UNIT.rem)
					margin = CssSize(1, UNIT.rem)

					h4("Open existing project")
					p("Open an existing ProjectRoot.xml file")
					button("Open project...") {
						align = Align.RIGHT

						onClick {
							scope.launch {
								val project = Services.startPageService.browseExistingProject()
								openProject(project)
							}
						}
					}
				}

				div {
					addCssClass("jumbotron")
					padding = CssSize(1, UNIT.rem)
					margin = CssSize(1, UNIT.rem)

					h4("Create new Project")
					p("Create a new ProjectRoot.xml file")
					button("New project...") {
						align = Align.RIGHT

						onClick {
							scope.launch {
								val project = Services.startPageService.createNewProject()
								openProject(project)
							}
						}
					}
				}

				div {
					addCssClass("jumbotron")
					padding = CssSize(1, UNIT.rem)
					margin = CssSize(1, UNIT.rem)

					h4("Documentation and changes")
					p("See the documentation and view the changelog")
					button("Documentation", style = ButtonStyle.SECONDARY) {
						align = Align.RIGHT
					}
				}
			}

			// recent
			h3("Recent Projects")

			recentProjectsDiv = div()
			updateRecentProjects()

		}
	}

	override fun canClose(): Boolean
	{
		return false
	}

	private fun updateRecentProjects() {
		scope.launch {
			val recentProjects = Services.startPageService.getRecentProjects()

			recentProjectsDiv.removeAll()

			if (recentProjects.isEmpty()) {
				recentProjectsDiv.add(Span("No recent projects found"))
			}

			for (project in recentProjects) {
				recentProjectsDiv.add(Div {
					addCssClass("jumbotron")
					padding = CssSize(1, UNIT.rem)
					margin = CssSize(1, UNIT.rem)
					width = CssSize(100, UNIT.perc)

					hPanel {
						vPanel {
							h4(project.path.split("/").last())
							span(project.path)
						}

						div {
							width = CssSize(100, UNIT.perc)
						}

						span(project.lastOpened.toDateString())
						button("X", style = ButtonStyle.SECONDARY) {
							onClick { e ->
								e.stopPropagation()
								scope.launch {
									Services.startPageService.removeRecentProject(project.path)
									updateRecentProjects()
								}
							}
						}
					}

					onClick {
						scope.launch {
							val project = Services.startPageService.openProject(project.path)
							openProject(project)
						}
					}
				})
			}
		}
	}

	private fun openProject(project: Project) {
		val projectPage = ProjectExplorerPage(project, pageManager)
		pageManager.addPage(projectPage)
		projectPage.show()
	}
}