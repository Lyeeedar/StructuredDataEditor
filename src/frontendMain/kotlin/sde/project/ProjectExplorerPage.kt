package sde.project

import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.Side
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.dockPanel
import pl.treksoft.kvision.toast.Toast
import pl.treksoft.kvision.toast.ToastOptions
import sde.Services
import sde.data.DataDocument
import sde.data.DataDocumentPage
import sde.data.Project
import sde.data.item.CompoundDataItem
import sde.pages.AbstractPage
import sde.pages.PageManager
import sde.pages.StartPage
import sde.ui.TextBlock
import sde.ui.mouseOverBackgroundColour
import sde.util.ProjectItem
import sde.utils.afterInsert
import sde.utils.disableSelection
import sde.utils.getFileDefType


class ProjectExplorerPage(val projectDef: ProjectDef, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "${projectDef.name} Project"

	override val closeable: Boolean
		get() = true

	val project = Project(projectDef, this)

	val projectRoot: ProjectFolderView

	init {
		val rootItem = ProjectItem()
		rootItem.path = projectDef.projectRootPath.split('/', '\\').dropLast(1).joinToString("/")
		rootItem.isDirectory = true

		projectRoot = ProjectFolderView(rootItem, this@ProjectExplorerPage)

		launch {
			project.loadJob?.join()

			if (project.definitionLoadErrors.size > 0) {
				var message = ""
				for (error in project.definitionLoadErrors.groupBy { it.first }) {
					message += error.key.split(projectDef.defsFolder)[1] + ":\n" + error.value.joinToString("\n") { it.second } + "\n"
				}

				Toast.error(message,"Some definitions failed to load", ToastOptions(timeOut = 100000))
			}
		}
	}

	private fun getVisibleItems(current: ProjectFolderView = projectRoot, depth: Int = 1): Sequence<AbstractProjectItemView>
	{
		return sequence {
			yield(current)

			val children = current.getChildrenIfVisible()
			if (children != null) {
				for (child in children) {
					child.depth = depth

					if (child is ProjectFolderView) {
						for (item in getVisibleItems(child, depth+1)) {
							yield(item)
						}
					} else {
						yield(child)
					}
				}
			}
		}
	}

	private val component = Div()
	fun updateComponent() {
		component.removeAll()

		component.add(VPanel {
			for (item in getVisibleItems()) {
				val component = item.getComponentCached()
				add(component)
			}
		})
	}

	override fun createComponent(): Component
	{
		updateComponent()
		return component
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