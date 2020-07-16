package sde.utils

import com.github.snabbdom.VNode
import pl.treksoft.jquery.JQuery
import pl.treksoft.kvision.core.Widget

fun Widget.hover(funcIn: (JQuery)->Unit, funcOut: (JQuery)->Unit): Widget {
	afterInsertHook = {
		val el = getElementJQuery()
		el?.hover({ funcIn(el) }, { funcOut(el) })
	}

	return this
}

fun Widget.afterInsert(handler: (VNode)->Unit): Widget
{
	afterInsertHook = handler
	return this
}