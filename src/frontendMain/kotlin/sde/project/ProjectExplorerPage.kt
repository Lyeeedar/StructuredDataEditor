package sde.project

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.panel.*
import pl.treksoft.kvision.toast.Toast
import pl.treksoft.kvision.toast.ToastOptions
import sde.data.Project
import sde.data.definition.CoreDefinitions
import sde.pages.AbstractPage
import sde.pages.PageManager
import sde.pages.StartPage
import sde.ui.textBlock
import sde.util.ProjectItem
import sde.utils.*


class ProjectExplorerPage(val projectDef: ProjectDef, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "${projectDef.name} Project"

	override val closeable: Boolean
		get() = true

	val project = Project(projectDef, this)

	val projectRoot: ProjectFolderView

	val failedLoadBanner = Div()

	init {
		val rootItem = ProjectItem()
		rootItem.path = projectDef.projectRootPath.split('/', '\\').dropLast(1).joinToString("/")
		rootItem.isDirectory = true

		projectRoot = ProjectFolderView(rootItem, this@ProjectExplorerPage)

		loadDefinitions()
	}

	private fun loadDefinitions() {
		failedLoadBanner.removeAll()

		launch {
			project.loadJob?.join()

			if (project.definitionLoadErrors.size > 0) {
				var message = ""
				for (error in project.definitionLoadErrors.groupBy { it.first }) {
					message += error.key.getFileNameWithoutExtension() + "<br />"
				}

				Toast.error(message,"Some definitions failed to load", ToastOptions(timeOut = 100000))

				failedLoadBanner.add(HPanel {
					background = Background(Color("red"))

					textBlock("Some definitions failed to load, click for more details") {
						margin = CssSize(5, UNIT.px)
					}
					button("Show errors", style = ButtonStyle.DANGER) {
						onClick {
							val modal = Modal {
								for (error in project.definitionLoadErrors.groupBy { it.first }) {
									val title = error.key.getFileNameWithoutExtension()
									h3(title)
									for (errorMessage in error.value) {
										p(errorMessage.second)
									}
								}
							}
							modal.show()
						}
					}
				})
			}
		}
	}

	private fun getVisibleItems(current: ProjectFolderView = projectRoot, depth: Int = 1): Sequence<AbstractProjectItemView>
	{
		return sequence {
			yield(current)

			val children = current.getChildrenIfVisible()
			if (children != null) {
				val files = ArrayList<ProjectFileView>()

				for (child in children) {
					child.depth = depth

					if (child is ProjectFolderView) {
						for (item in getVisibleItems(child, depth+1)) {
							yield(item)
						}
					} else if (child is ProjectFileView) {
						files.add(child)
					}
				}

				for (item in files) {
					yield(item)
				}
			}
		}
	}

	override fun createTabHeader(): Component {
		return VPanel {
			bold(projectDef.name)
			span("Project Explorer") {
				opacity = 0.8
			}

			afterInsert {
				it.disableSelection()
			}
		}
	}

	private val projectItemsComponent = Div()
	fun updateProjectItemsComponent() {
		projectItemsComponent.removeAll()

		projectItemsComponent.add(VPanel {
			for (item in getVisibleItems()) {
				val component = item.getComponentCached()
				add(component)
			}
		})
	}

	override fun createComponent(): Component
	{
		updateProjectItemsComponent()
		return GridPanel(templateRows = "auto auto 1fr") {
			add(failedLoadBanner, 1, 1)

			add(HPanel(wrap = FlexWrap.WRAP) {
				div {
					addCssClass("jumbotron")
					padding = CssSize(1, UNIT.rem)
					margin = CssSize(1, UNIT.rem)

					h4("Create definition")
					p("Create a new .xmldef file")
					button("Create def...") {
						align = Align.RIGHT

						onClick {
							val def = CoreDefinitions.rootDef
							launch {
								project.create(def, "")
							}
						}
					}
				}

				vPanel {
					addCssClass("jumbotron")
					padding = CssSize(1, UNIT.rem)
					margin = CssSize(1, UNIT.rem)

					h4("Create data")
					p("Create a new data file")

					launch {
						project.loadJob?.join()

						for (root in project.rootDefinitions) {
							button("Create ${root.key} file...") {
								onClick {
									val def = root.value
									launch {
										project.create(def, "")
									}
								}
							}
						}
					}
				}
			}, 1, 2)

			add(projectItemsComponent, 1, 3)
		}
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