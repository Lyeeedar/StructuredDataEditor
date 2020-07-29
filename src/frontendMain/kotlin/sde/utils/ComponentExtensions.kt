package sde.utils

import com.github.snabbdom.VNode
import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.w3c.dom.ImageBitmap
import org.w3c.dom.ImageBitmapOptions
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReaderSync
import pl.treksoft.jquery.JQuery
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.html.Image
import sde.Services
import kotlin.browser.window

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

suspend fun imageFromFile(path: String, init: (Image.()->Unit)? = null): Image
{
	val dataUrl = ImageCache.getImageUrl(path)

	return Image(dataUrl).apply {
		init?.invoke(this)
	}
}

object ImageCache {
	private val mutex = Mutex()
	private val imageBlobCache = HashMap<String, Blob>()
	private val imageUrlCache = HashMap<String, String>()
	private val imageBitmapCache = HashMap<String, ImageBitmap>()

	private suspend fun getImageBlob(path: String): Blob {
		val existing = imageBlobCache[path]
		if (existing != null) {
			return existing
		}

		mutex.withLock {
			val existing = imageBlobCache[path]
			if (existing != null) {
				return existing
			}

			val bytes = Services.disk.loadFileBytes(path)
			val fileType = path.split('.').last()
			val blob = Blob(arrayOf(bytes.toByteArray()), BlobPropertyBag(type = "image/$fileType"))
			imageBlobCache[path] = blob

			return blob
		}
	}

	suspend fun getImageBitmap(path: String): ImageBitmap {
		val existing = imageBitmapCache[path]
		if (existing != null) {
			return existing
		}

		val blob = getImageBlob(path)
		val image = window.createImageBitmap(blob, ImageBitmapOptions()).await()
		imageBitmapCache[path] = image

		return image
	}

	suspend fun getImageUrl(path: String): String {
		val existing = imageUrlCache[path]
		if (existing != null) {
			return existing
		}

		val blob = getImageBlob(path)
		val dataUrl = URL.createObjectURL(blob)
		imageUrlCache[path] = dataUrl

		return dataUrl
	}
}