package sde.pages

import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.onClick
import pl.treksoft.kvision.html.*
import sde.Services
import sde.project.Project
import sde.project.ProjectItem

class ProjectExplorerPage(val project: Project, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "${project.name} Project"

	override val closeable: Boolean
		get() = true

	lateinit var projectRoot: ProjectFolderView

	override fun createComponent(): Component
	{
		val projectTreeDiv = Div {

		}

		val component = Div {
			add(projectTreeDiv)
		}

		scope.launch {
			val rootItem = ProjectItem()
			rootItem.path = project.projectRootPath.split('/', '\\').dropLast(1).joinToString("/")
			rootItem.isDirectory = true

			projectRoot = ProjectFolderView(rootItem, this@ProjectExplorerPage)
			val comp = projectRoot.getComponent()

			projectTreeDiv.add(comp)
		}

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

abstract class AbstractProjectItemView(val item: ProjectItem, val page: ProjectExplorerPage)
{
	abstract fun getComponent(): Component

	companion object
	{
		fun getItemView(item: ProjectItem, page: ProjectExplorerPage): AbstractProjectItemView
		{
			return when (item.isDirectory)
			{
				true -> ProjectFolderView(item, page)
				false -> ProjectFileView(item, page)
			}
		}
	}
}

class ProjectFolderView(item: ProjectItem, page: ProjectExplorerPage) : AbstractProjectItemView(item, page)
{
	val name = item.path.split('/', '\\').last().split('.').first()

	private val li: Li = Li()
	private var children: List<AbstractProjectItemView>? = null

	var isExpanded: Boolean = false
		set(value) {
			field = value

			page.scope.launch {
				loadChildren()
				updateComponent()
			}
		}

	init
	{
		li.onClick { e ->
			isExpanded = !isExpanded
			e.stopPropagation()
		}
	}

	private suspend fun loadChildren() {
		if (children == null) {
			val items = Services.projectService.getFolderContents(item.path)
			children = items.map { getItemView(it, page) }.toList()
		}
	}

	private fun updateComponent() {
		li.removeAll()
		li.add(Bold(name))

		val children = children
		if (isExpanded && children != null) {
			li.add(Ul {
				for (item in children) {
					add(item.getComponent())
				}
			})
		}
	}

	override fun getComponent(): Component
	{
		updateComponent()
		return li
	}

}

class ProjectFileView(item: ProjectItem, page: ProjectExplorerPage) : AbstractProjectItemView(item, page)
{
	val name = item.path.split('/', '\\').last()

	override fun getComponent(): Component
	{
		return Li(name) {
			onClick { e ->
				e.stopPropagation()
			}
		}
	}

}