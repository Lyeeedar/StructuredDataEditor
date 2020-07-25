package sde.project

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.Side
import pl.treksoft.kvision.panel.dockPanel
import sde.Services
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
    val name = item.path.getFileName()

    private var children: List<AbstractProjectItemView>? = null

    var isExpanded: Boolean = false
        set(value) {
            field = value
            component = null
            page.updateProjectItemsComponent()

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

            page.updateProjectItemsComponent()
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
    val name = item.path.getFileName()
    var type: String? = null

    suspend fun openFile() {
        page.project.open(item.path)
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
        if (item.path.endsWith(".xmldef")) {
            name.color = Color("#acb5ad")

            val newImage = Image(pl.treksoft.kvision.require("images/DefIcon.png") as? String).apply {
                width = CssSize(16, UNIT.px)
                height = CssSize(16, UNIT.px)
            }
            icon.removeAll()
            icon.add(newImage)
        } else if (page.project.supportedExtensions.contains(item.path.getExtension()))
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
                            val newImage = imageFromFile(page.project.projectRootFolder + "/" + def.fileIcon) {
                                width = CssSize(16, UNIT.px)
                                height = CssSize(16, UNIT.px)
                            }
                            icon.removeAll()
                            icon.add(newImage)
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
                    page.launch {
                        openFile()
                    }
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