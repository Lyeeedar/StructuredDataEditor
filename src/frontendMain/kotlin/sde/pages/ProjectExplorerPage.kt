package sde.pages

import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.onClick
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.hPanel
import sde.Services
import sde.data.DataDocument
import sde.data.Project
import sde.data.item.CompoundDataItem
import sde.project.ProjectDef
import sde.project.ProjectItem
import sde.utils.UndoRedoManager

class ProjectExplorerPage(val projectDef: ProjectDef, pageManager: PageManager) : AbstractPage(pageManager)
{
	override val name: String
		get() = "${projectDef.name} Project"

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
			rootItem.path = projectDef.projectRootPath.split('/', '\\').dropLast(1).joinToString("/")
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
	var type: String? = null

	override fun getComponent(): Component
	{
		val typeSpan = Span {
			align = Align.RIGHT
		}
		val li = Li {
			hPanel {
				span(name)
				add(typeSpan)
			}

			onClick { e ->
				e.stopPropagation()

				val xml = """
					<Definitions xmlns:meta="Editor">
						<Data Name="Block" meta:RefKey="Struct">
							<Data Name="Count1" meta:RefKey="Number" />
							<Data Name="Block" meta:RefKey="Struct">
								<Data Name="Count1" meta:RefKey="Number" />
								<Data Name="Count4" meta:RefKey="Number" />
							</Data>
							<Data Name="Count2" meta:RefKey="Number" />
						</Data>
					</Definitions>
				""".trimIndent()
				val defMap = Project.parseDefinitionsFile(xml, "")
				val def = defMap["Block"]!!

				val data = DataDocument()
				val item = def.createItem(data)


				data.root = item as CompoundDataItem

				val page = DataDocumentPage(data, page.pageManager)
				page.pageManager.addPage(page)
				page.show()
			}
		}

		if (item.path.endsWith("xml"))
		{
			if (type == null)
			{
				page.scope.launch {
					type = Services.projectService.getFileDefType(item.path)
					typeSpan.content = "($type)"
				}
			}
			else
			{
				typeSpan.content = "($type)"
			}
		}

		return li
	}

}