package sde.utils

import org.w3c.dom.*
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.parsing.XMLSerializer

fun String.parseXml(): XMLDocument
{
	val parser = DOMParser()
	return parser.parseFromString(this, "application/xml") as XMLDocument
}

fun Node.serializeXml(): String
{
	val serializer = XMLSerializer()
	return serializer.serializeToString(this)
}

fun XMLDocument.getElement(name: String): Element?
{
	return this.childNodes.elements().first().getElement(name)
}

fun Element.getElement(name: String): Element?
{
	return this.childNodes.elements().firstOrNull { it.nodeName == name }
}

fun Element.getElementValue(name: String, fallback: String): String
{
	return this.getElement(name)?.textContent ?: fallback
}

fun Element.getAttributeValue(name: String, fallback: String): String
{
	return this.getAttribute(name) ?: fallback
}

fun Element.getAttributeValue(name: String, fallback: Boolean): Boolean
{
	return this.getAttribute(name)?.toBoolean() ?: fallback
}

fun Element.getAttributeValue(name: String, fallback: Int): Int
{
	return this.getAttribute(name)?.toInt() ?: fallback
}

fun Element.getAttributeValue(name: String, fallback: Float): Float
{
	return this.getAttribute(name)?.toFloat() ?: fallback
}

fun NodeList.elements(): Sequence<Element>
{
	return this.getType(Node.ELEMENT_NODE).map { it as Element }
}

fun NodeList.attributes(): Sequence<Attr>
{
	return this.getType(Node.ATTRIBUTE_NODE).map { it as Attr }
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