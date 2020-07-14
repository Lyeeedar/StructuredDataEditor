package sde.utils

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.XMLDocument
import org.w3c.dom.parsing.DOMParser

fun String.parseXml(): XMLDocument
{
	val parser = DOMParser()
	return parser.parseFromString(this, "application/xml") as XMLDocument
}

fun XMLDocument.getElement(name: String): Node?
{
	return this.childNodes.elements().first().getElement(name)
}

fun Node.getElement(name: String): Node?
{
	return this.childNodes.elements().firstOrNull { it.nodeName == name }
}

fun Node.getElementValue(name: String, fallback: String): String
{
	return this.getElement(name)?.textContent ?: fallback
}

fun Node.getAttribute(name: String): Node?
{
	return this.childNodes.attributes().firstOrNull{ it.nodeName == name }
}

fun Node.getAttributeValue(name: String, fallback: String): String
{
	return this.getAttribute(name)?.textContent ?: fallback
}

fun Node.getAttributeValue(name: String, fallback: Boolean): Boolean
{
	return this.getAttribute(name)?.textContent?.toBoolean() ?: fallback
}

fun Node.getAttributeValue(name: String, fallback: Int): Int
{
	return this.getAttribute(name)?.textContent?.toInt() ?: fallback
}

fun Node.getAttributeValue(name: String, fallback: Float): Float
{
	return this.getAttribute(name)?.textContent?.toFloat() ?: fallback
}

fun NodeList.elements(): Sequence<Node>
{
	return this.getType(Node.ELEMENT_NODE)
}

fun NodeList.attributes(): Sequence<Node>
{
	return this.getType(Node.ATTRIBUTE_NODE)
}

fun NodeList.getType(type: Short): Sequence<Node>
{
	return this.asSequence().filter { it.nodeType == type }
}

fun NodeList.asSequence(): Sequence<Node>
{
	val list = this
	return sequence {
		for (i in 0 until list.length) {
			val child = list.item(i) ?: continue
			yield(child)
		}
	}
}