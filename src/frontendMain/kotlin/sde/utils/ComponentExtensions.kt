package sde.utils

import com.github.snabbdom.VNode
import pl.treksoft.jquery.JQuery
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.core.onEvent

fun JQuery.hover(funcIn: (JQuery)->Unit, funcOut: (JQuery)->Unit)
{
	val el = this
	el.hover({ funcIn(el) }, { funcOut(el) })
}

fun JQuery.disableSelection()
{
	css("user-select", "none")
}

fun Widget.afterInsert(actionFunc: (JQuery)->Unit)
{
	afterInsertHook = {
		val el = getElementJQuery()
		if (el != null) actionFunc(el)
	}
}