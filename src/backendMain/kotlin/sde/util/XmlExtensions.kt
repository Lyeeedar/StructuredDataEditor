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

fun Node.getElement(name: String): Node? {
	for (i in 0 until this.childNodes.length) {
		val child = this.childNodes.item(i)

		if (child.nodeName == name) {
			return child
		}
	}

	return null
}

val Document.root: Node
	get() = this.childNodes.item(0)

fun Document.getElement(name: String): Node? {
	return this.root.getElement(name)
}

var Node.value: String
	get() = this.textContent
	set(value) {
		this.textContent = value
	}