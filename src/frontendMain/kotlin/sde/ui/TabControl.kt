package sde.ui

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Bold
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.Side
import sde.utils.afterInsert
import sde.utils.disableSelection
import sde.utils.hover

class TabControl : DockPanel()
{
    val tabsPanel = HPanel()
    val bodyPanel = Div()
    var currentTab: Tab? = null
    val tabs = ArrayList<Tab>()

    init {
        add(tabsPanel, Side.UP)
        add(bodyPanel)
    }

    fun addTab(header: String, body: Component, key: Any, closeable: Boolean = true, onClosing: (()->Boolean)? = null, onClosed: (()->Unit)? = null, onFocused: (()->Unit)? = null) {
        addTab(Bold(header), body, key, closeable, onClosing, onClosed, onFocused)
    }

    fun addTab(header: Component, body: Component, key: Any, closeable: Boolean = true, onClosing: (()->Boolean)? = null, onClosed: (()->Unit)? = null, onFocused: (()->Unit)? = null) {
        val tab = Tab(header, body, key, closeable, onClosing, onClosed, onFocused)
        tab.tabHeader = DockPanel {
            margin = CssSize(1, UNIT.px)
            padding = CssSize(3, UNIT.px)

            border = Border(CssSize(1, UNIT.px), BorderStyle.SOLID, borderNormalColour)
            background = Background(backgroundNormalColour)

            afterInsert {
                val el = it
                el.disableSelection()
                el.hover({ el.css("border-color", mouseOverBorderColour.toString()) }, { el.css("border-color", borderNormalColour.asString()) })
            }

            if (closeable) {
                add(ImageButton(pl.treksoft.kvision.require("images/Remove.png") as? String) {
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