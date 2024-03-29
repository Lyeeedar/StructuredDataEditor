package sde.pages

import kotlinx.serialization.Serializable
import io.kvision.core.*
import io.kvision.form.*
import io.kvision.form.text.Text
import io.kvision.html.*
import io.kvision.modal.Modal
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import sde.Services
import sde.project.ProjectDef
import sde.project.ProjectExplorerPage
import sde.project.addRecentProject
import sde.project.removeRecentProject
import sde.ui.TextBlock
import sde.ui.textBlock
import sde.utils.afterInsert
import sde.utils.disableSelection
import sde.utils.getProjectName
import sde.utils.updateSettings
import kotlin.js.Date

class StartPage(pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "Start Page"

	override val closeable: Boolean
		get() = false

	lateinit var recentProjectsDiv: Div

	override fun createTabHeader(): Component {
		return B("Start Page") {
			afterInsert {
				it.disableSelection()
			}
		}
	}

	override fun createComponent(): Component
	{
		return Div {
			id = "StartPage"

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
							launch {
								val file = Services.disk.browseFile()
								val project = ProjectDef.load(file)
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
											launch {
												val folder = Services.disk.browseFolder()

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
												launch {
													val data = form.getData()
													val contents = data.toString()
													val path = data.rootFolder + "/ProjectRoot.xml"

													Services.disk.saveFileString(path, contents)

													val project = ProjectDef.load(path)
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
		launch {
			val settings = Services.settings.loadSettings()
			val recentProjects = settings.recentProjects

			recentProjectsDiv.removeAll()

			if (recentProjects.isEmpty()) {
				recentProjectsDiv.add(TextBlock("No recent projects found"))
			}

			for (project in recentProjects) {
				val projName = project.getProjectName()

				recentProjectsDiv.add(Div {
					addCssClass("jumbotron")
					padding = CssSize(1, UNIT.rem)
					margin = CssSize(1, UNIT.rem)
					width = CssSize(100, UNIT.perc)

					hPanel {
						vPanel {
							h4(projName)
							textBlock(project.path)
						}

						textBlock(Date(project.lastOpened).toLocaleDateString())
						button("X", style = ButtonStyle.SECONDARY) {
							onClick { e ->
								e.stopPropagation()
								launch {
									updateSettings {
										it.removeRecentProject(project.path)
									}
									updateRecentProjects()
								}
							}
						}
					}

					onClick {
						launch {
							val project = ProjectDef.load(project.path)
							openProject(project)
						}
					}
				})
			}
		}
	}

	private fun openProject(projectDef: ProjectDef) {
		launch {
			updateSettings {
				it.addRecentProject(projectDef.projectRootPath)
			}
		}

		val projectPage = ProjectExplorerPage(projectDef, pageManager)
		pageManager.pages.remove(this)
		pageManager.addPage(projectPage)
		projectPage.show()
	}
}

@Serializable
class NewProjectConfig(var rootFolder: String = "", var defsFolder: String = "", var name: String = "")
{
	override fun toString(): String {
		return """
			<Project>
				<Name>$name</Name>
				<Definitions>$defsFolder</Definitions>
			</Project>
		""".trimIndent()
	}
}