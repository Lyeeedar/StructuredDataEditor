package sde.project

import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.Side
import pl.treksoft.kvision.panel.dockPanel
import sde.Services
import sde.data.DataDocument
import sde.data.DataDocumentPage
import sde.data.Project
import sde.data.item.CompoundDataItem
import sde.ui.TextBlock
import sde.ui.mouseOverBackgroundColour
import sde.util.ProjectItem
import sde.utils.*

abstract class AbstractProjectItemView(val item: ProjectItem, val page: ProjectExplorerPage)
{
    var depth = 0

    var wrapperDiv = Div()
    init {
        wrapperDiv.afterInsert {
            val el = it
            it.hover({
                el.css("background-color", mouseOverBackgroundColour.asString())
            }, {
                el.css("background-color", "transparent")
            })
        }
    }

    protected var component: Component? = null
    fun getComponentCached(): Component
    {
        if (component == null) {
            wrapperDiv.removeAll()
            wrapperDiv.add(getComponent())

            component = wrapperDiv
        }

        return component!!
    }

    protected abstract fun getComponent(): Component

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

    private var children: List<AbstractProjectItemView>? = null

    var isExpanded: Boolean = false
        set(value) {
            field = value
            component = null
            page.updateComponent()

            page.launch {
                loadChildren()
            }
        }

    fun getChildrenIfVisible(): List<AbstractProjectItemView>?
    {
        if (isExpanded) {
            return children ?: listOf(LoadingView(item, page))
        }

        return null
    }

    private suspend fun loadChildren() {
        if (children == null) {
            val items = Services.disk.getFolderContents(item.path)
            children = items.map { getItemView(it, page) }.toList()

            page.updateComponent()
        }
    }

    override fun getComponent(): Component
    {
        return Div {
            dockPanel {
                marginLeft = CssSize(depth * 20, UNIT.px)

                if (isExpanded)
                {
                    add(Image(pl.treksoft.kvision.require("images/OpenArrow.png") as? String), Side.LEFT)
                }
                else
                {
                    add(Image(pl.treksoft.kvision.require("images/RightArrow.png") as? String), Side.LEFT)
                }

                add(Bold(name) {
                    opacity = 0.7

                    afterInsert {
                        it.disableSelection()
                    }
                })
            }

            onClick { e ->
                isExpanded = !isExpanded
                e.stopPropagation()
            }
        }
    }

}

class ProjectFileView(item: ProjectItem, page: ProjectExplorerPage) : AbstractProjectItemView(item, page)
{
    val name = item.path.split('/', '\\').last()
    var type: String? = null

    fun openFile() {
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

    override fun getComponent(): Component
    {
        val typeSpan = TextBlock {
            align = Align.RIGHT
            opacity = 0.5
        }
        val icon = Div() {
            width = CssSize(16, UNIT.px)
            height = CssSize(16, UNIT.px)
            maxWidth = CssSize(16, UNIT.px)
            maxHeight = CssSize(16, UNIT.px)

            afterInsert {
                it.disableSelection()
            }

            image(pl.treksoft.kvision.require("images/File.png") as? String) {
                width = CssSize(16, UNIT.px)
                height = CssSize(16, UNIT.px)
            }
        }
        val name = TextBlock(name)
        if (item.path.endsWith("xml"))
        {
            if (type == null)
            {
                page.launch {
                    page.project.loadJob?.join()

                    type = item.path.getFileDefType()
                    typeSpan.content = "($type)"

                    val def = page.project.rootDefinitions[type]
                    if (def != null) {
                        if (def.fileColour.isNotBlank()) {
                            name.color = Color("rgb(${def.fileColour})")
                        }

                        if (def.fileIcon.isNotBlank()) {
                            icon.removeAll()
                            icon.add(imageFromFile(page.project.projectRootFolder + "/" + def.fileIcon) {
                                width = CssSize(16, UNIT.px)
                                height = CssSize(16, UNIT.px)
                            })
                        }
                    } else {
                        name.color = Color("rgb(255,0,0)")
                    }
                }
            }
            else
            {
                typeSpan.content = "($type)"
            }
        }

        return DockPanel {
            marginLeft = CssSize(depth * 20 + 10, UNIT.px)

            add(typeSpan, Side.RIGHT)
            add(icon, Side.LEFT)
            add(name)

            afterInsert {
                it.dblclick {
                    openFile()
                }
            }
        }
    }

}

class LoadingView(item: ProjectItem, page: ProjectExplorerPage) : AbstractProjectItemView(item, page) {
    override fun getComponent(): Component {
        return TextBlock("... Loading ...") {
            marginLeft = CssSize(depth * 20 + 10, UNIT.px)
            opacity = 0.5
        }
    }
}