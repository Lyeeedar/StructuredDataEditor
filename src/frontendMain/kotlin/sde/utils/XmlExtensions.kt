package sde.utils

import org.w3c.dom.*
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.parsing.XMLSerializer
import sde.Services
import sde.util.*

suspend fun String.getFileDefType(): String?
{
	try {
		val fileContents = Services.disk.loadFileString(this)
		val xml = fileContents.parseXml()
		return xml.childNodes.item(0)!!.nodeName
	} catch(ex: Exception) {
		return null
	}
}

fun String.parseXml(): XMLDocument
{
	val parser = DOMParser()
	return parser.parseFromString(this, "application/xml") as XMLDocument
}

fun XMLDocument.toXDocument(): XDocument
{
	val root = this.childNodes.item(0)!!
	val xroot = root.toXData() as XElement

	val doc = XDocument()
	doc.root = xroot

	return doc
}

fun Node.toXData(): XData?
{
	println(this)

	val node = this
	return when (this.nodeType)
	{
		Node.ELEMENT_NODE -> {
			XElement().apply {
				val el = node as Element
				this.name = node.nodeName

				for (i in 0 until node.childNodes.length)
				{
					val childNode = node.childNodes.item(i)!!

					val xdata = childNode.toXData()

					if (xdata == null) continue
					else if (xdata is XAttribute) this.attributes.add(xdata)
					else this.children.add(xdata)
				}

				val attNames = el.getAttributeNames()
				for (att in attNames)
				{
					val value = el.getAttribute(att)!!
					this.attributes.add(XAttribute().apply {
						this.name = att
						this.value = value
					})
				}

				if (this.children.size == 0)
				{
					this.value = node.textContent!!
				}
			}
		}
		Node.ATTRIBUTE_NODE -> {
			XAttribute().apply {
				this.name = node.nodeName
				this.value = node.textContent!!
			}
		}
		Node.COMMENT_NODE -> {
			XComment().apply {
				this.text = node.textContent!!
			}
		}
		else -> null
	}
}