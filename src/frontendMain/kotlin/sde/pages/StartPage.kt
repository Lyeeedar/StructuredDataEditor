package sde.pages

import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.*
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.panel.vPanel
import sde.NewProjectConfig
import sde.Services
import sde.project.ProjectDef

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
							val modal = Modal(caption = "New project")
							modal.add(vPanel {
								formPanel<NewProjectConfig> {
									val form = this

									this.add(NewProjectConfig::name, Text(label = "Project name"), required = true, validator = { !it.value.isNullOrBlank() }, validatorMessage = { "Project name must not be empty" })
									this.add(NewProjectConfig::rootFolder, Text(label = "Root folder"), required = true, validator = { !it.value.isNullOrBlank() }, validatorMessage = { "Root folder must not be empty" })
									button("Browse", style = ButtonStyle.SECONDARY) {
										onClick {
											scope.launch {
												val folder = Services.startPageService.browseFolder()

												val name = form[NewProjectConfig::name] as String? ?: ""
												val currentData = NewProjectConfig(folder, "$folder/Definitions", name)
												form.setData(currentData)
											}
										}
									}
									this.add(NewProjectConfig::defsFolder, Text(label = "Definitions folder"), required = true, validator = { !it.value.isNullOrBlank() }, validatorMessage = { "Definitions folder must not be empty" })

									button("Create") {
										onClick {
											if (form.validate(true)) {
												scope.launch {
													val project = Services.startPageService.createNewProject(form.getData())
													openProject(project)

													modal.hide()
												}
											}
										}
									}
								}
							})
							modal.show()


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
							h4(project.name)
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

	private fun openProject(projectDef: ProjectDef) {
		val projectPage = ProjectExplorerPage(projectDef, pageManager)
		pageManager.pages.remove(this)
		pageManager.addPage(projectPage)
		projectPage.show()
	}
}