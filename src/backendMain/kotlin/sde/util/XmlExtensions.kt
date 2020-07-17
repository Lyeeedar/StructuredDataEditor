package sde.util

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun String.parseXml(): Document {
	val dbFactory = DocumentBuilderFactory.newInstance()
	val dBuilder = dbFactory.newDocumentBuilder()
	val xmlInput = InputSource(StringReader(this))
	return dBuilder.parse(xmlInput)
}

fun Document.toXDocument(): XDocument
{
	val root = this.childNodes.item(0)
	val xroot = root.toXData() as XElement

	val doc = XDocument()
	doc.root = xroot

	return doc
}

fun Node.toXData(): XData?
{
	val node = this
	return when (this.nodeType)
	{
		Node.ELEMENT_NODE -> {
			XElement().apply {
				this.name = node.nodeName

				for (i in 0 until node.childNodes.length)
				{
					val childNode = node.childNodes.item(i)
					val xdata = childNode.toXData()

					if (xdata == null) continue
					else if (xdata is XAttribute) this.attributes.add(xdata)
					else this.children.add(xdata)
				}

				if (this.children.size == 0)
				{
					this.value = node.textContent
				}
			}
		}
		Node.ATTRIBUTE_NODE -> {
			XAttribute().apply {
				this.name = node.nodeName
				this.value = node.textContent
			}
		}
		Node.COMMENT_NODE -> {
			XComment().apply {
				this.text = node.textContent
			}
		}
		else -> null
	}
}