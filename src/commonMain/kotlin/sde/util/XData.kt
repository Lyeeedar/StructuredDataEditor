package sde.util

import kotlinx.serialization.Serializable

@Serializable
class XDocument
{
	lateinit var root: XElement

	override fun toString(): String
	{
		val holder = ArrayList<String>()
		root.write(holder, 0)
		val output = holder.joinToString("\n")

		return output.trim()
	}
}

@Serializable
abstract class XData
{
	abstract fun write(output: ArrayList<String>, indentation: Int)

	override fun toString(): String
	{
		val holder = ArrayList<String>()
		write(holder, 0)
		val output = holder.joinToString("\n")

		return output.trim()
	}
}

@Serializable
class XComment() : XData()
{
	var text: String = ""

	override fun write(output: ArrayList<String>, indentation: Int)
	{
		val indentation = "\t".repeat(indentation)
		output.add("$indentation<!--$text-->")
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

	override fun write(output: ArrayList<String>, indentation: Int)
	{
		val indentationStr = "\t".repeat(indentation)

		var attributesStr = ""
		if (attributes.size > 0) {
			val attHolder = ArrayList<String>()
			attributes.forEach { it.write(attHolder, 0) }

			attributesStr = " " + attHolder.joinToString(" ")
		}

		if (children.size == 0 && value.isBlank()) {
			output.add("$indentationStr<$name$attributesStr />")
		} else if (children.size == 0) {
			output.add("$indentationStr<$name$attributesStr>$value</$name>")
		} else {
			output.add("$indentationStr<$name$attributesStr>")
			for (child in children)
			{
				child.write(output, indentation+1)
			}
			output.add("$indentationStr</$name>")
		}
	}
}

@Serializable
class XAttribute : XData()
{
	var name: String = ""
	var value: String = ""

	override fun write(output: ArrayList<String>, indentation: Int)
	{
		output.add("$name=\"$value\"")
	}
}