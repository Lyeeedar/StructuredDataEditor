package sde.utils

import com.github.snabbdom.VNode
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReaderSync
import pl.treksoft.jquery.JQuery
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.html.Image

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

fun imageFromBytes(bytes: List<Byte>, originalFileType: String, init: (Image.()->Unit)? = null): Image
{
	val blob = Blob(arrayOf(bytes.toByteArray()), BlobPropertyBag(type = "image/$originalFileType"))
	val dataUrl = URL.createObjectURL(blob)

	return Image(dataUrl).apply {
		init?.invoke(this)
	}
}