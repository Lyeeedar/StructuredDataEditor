package sde.ui

import io.kvision.core.*
import io.kvision.html.B
import io.kvision.html.Button
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.panel.DockPanel
import io.kvision.panel.HPanel
import io.kvision.panel.Side
import sde.utils.afterInsert
import sde.utils.disableSelection
import sde.utils.hover

class TabControl : DockPanel()
{
    val tabsPanel = HPanel() {
        width = CssSize(100, UNIT.perc)
        border = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderDarkColour)
    }
    val bodyPanel = Div() {
	    id = "TabBody"
        width = CssSize(100, UNIT.perc)
        height = CssSize(100, UNIT.perc)
        border = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderDarkColour)
    }

    var currentTab: Tab? = null
    val tabs = ArrayList<Tab>()

    init {
	    id = "TabControl"

        width = CssSize(100, UNIT.perc)
        height = CssSize(100, UNIT.perc)

        add(tabsPanel, Side.UP)
        add(bodyPanel)
    }

    fun addTab(header: String, body: Component, key: Any, closeable: Boolean = true, onClosing: (()->Boolean)? = null, onClosed: (()->Unit)? = null, onFocused: (()->Unit)? = null) {
        addTab(B(header), body, key, closeable, onClosing, onClosed, onFocused)
    }

    fun addTab(header: Component, body: Component, key: Any, closeable: Boolean = true, onClosing: (()->Boolean)? = null, onClosed: (()->Unit)? = null, onFocused: (()->Unit)? = null) {
        val tab = Tab(header, body, key, closeable, onClosing, onClosed, onFocused)
        tab.tabHeader = DockPanel {
	        id = "TabHeader"

            marginTop = CssSize(1, UNIT.px)
            marginLeft = CssSize(1, UNIT.px)
            marginRight = CssSize(1, UNIT.px)
            padding = CssSize(3, UNIT.px)

            border = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderNormalColour)
            background = Background(backgroundNormalColour)

            afterInsert {
                val el = it
                el.disableSelection()
                el.hover({ el.css("border-color", mouseOverBorderColour.toString()) }, { el.css("border-color", borderNormalColour.asString()) })
            }

            if (closeable) {
                add(ImageButton(io.kvision.require("images/Remove.png") as? String) {
                    backgroundCol = Color("transparent")
                    borderCol = Color("transparent")
                    marginLeft = CssSize(3, UNIT.px)

                    onClick { e ->
                        removeTab(key)

                        e.stopPropagation()
                    }
                }, Side.RIGHT)
            }

            onClick {
                selectTab(key)
            }

            add(header)
        }

        tabs.add(tab)

        tabsPanel.add(tab.tabHeader)

        if (currentTab == null) {
            selectTab(key)
        }
    }

    fun removeAllTabs() {
        tabs.clear()
        tabsPanel.removeAll()
        bodyPanel.removeAll()

        currentTab = null
    }

    fun removeTab(key: Any) {
        val tab = tabs.firstOrNull { it.key == key } ?: return
        if (tab.onClosing?.invoke() == false) return

        tabs.remove(tab)
        tabsPanel.remove(tab.tabHeader)

        if (currentTab == tab) {
            bodyPanel.removeAll()

            if (tabs.size > 0) {
                selectTab(tabs[0].key)
            }
        }

        tab.onClosed?.invoke()
    }

    fun selectTab(key: Any) {
        val tab = tabs.firstOrNull { it.key == key } ?: return

        currentTab?.isSelected = false
        currentTab?.updateBackgroundCol()

        currentTab = tab
        bodyPanel.removeAll()
        bodyPanel.add(tab.body)

        tab.onFocused?.invoke()

        tab.isSelected = true
        tab.updateBackgroundCol()
    }
}

class Tab(val header: Component, val body: Component, val key: Any, val closeable: Boolean, val onClosing: (()->Boolean)? = null, val onClosed: (()->Unit)? = null, val onFocused: (()->Unit)? = null)
{
    var isSelected = false

    lateinit var tabHeader: Component

    fun updateBackgroundCol() {
        val el = tabHeader.getElementJQuery() ?: return
        val col = if (isSelected) selectionBackgroundColour else backgroundNormalColour
        el.css("background-color", col.asString())
    }
}