package sde.util

import kotlinx.serialization.Serializable

@Serializable
class XDocument
{
	lateinit var root: XElement

	override fun toString(): String
	{
		return root.toString(0)
	}
}

@Serializable
abstract class XData
{
	abstract fun toString(indentation: Int): String
}

@Serializable
class XComment() : XData()
{
	var text: String = ""

	override fun toString(indentation: Int): String
	{
		return "${"\t".repeat(indentation)}<--$text-->"
	}
}

@Serializable
class XElement : XData()
{
	var name: String = ""
	var value: String = ""

	val children = ArrayList<XData>()
	val attributes = ArrayList<XAttribute>()

	fun getElement(name: String): XElement?
	{
		return children.filterIsInstance<XElement>().firstOrNull { it.name == name }
	}

	fun getAttribute(name: String): XAttribute?
	{
		return attributes.firstOrNull { it.name == name }
	}

	fun getElementValue(name: String, fallback: String): String
	{
		return this.getElement(name)?.value ?: fallback
	}

	fun getAttributeValue(name: String, fallback: String): String
	{
		return this.getAttribute(name)?.value ?: fallback
	}

	fun getAttributeValue(name: String, fallback: Boolean): Boolean
	{
		return this.getAttribute(name)?.value?.toBoolean() ?: fallback
	}

	fun getAttributeValue(name: String, fallback: Int): Int
	{
		return this.getAttribute(name)?.value?.toInt() ?: fallback
	}

	fun getAttributeValue(name: String, fallback: Float): Float
	{
		return this.getAttribute(name)?.value?.toFloat() ?: fallback
	}

	override fun toString(indentation: Int): String
	{
		val indent = "\t".repeat(indentation)

		var contents = if (children.size > 0) children.joinToString("\n") { indent + it.toString(indentation+1) } else value
		if (contents.contains("\n") || children.size > 0) contents = "\n$contents\n$indent"

		return """
			$indent<$name ${attributes.joinToString(" ") { it.toString(indentation) }}>$contents</$name>
		""".trimIndent()
	}
}

@Serializable
class XAttribute : XData()
{
	var name: String = ""
	var value: String = ""

	override fun toString(indentation: Int): String
	{
		return "$name=\"$value\""
	}
}