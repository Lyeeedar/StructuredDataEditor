package sde.utils

import kotlin.js.Date
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

fun String.getExtension(): String {
	val split = this.getFileName().split('.')
	if (split.size == 1) {
		return ""
	} else {
		return split.last()
	}
}

fun String.getFileName() = this.split('/', '\\').last()

fun String.getFileNameWithoutExtension() = this.getFileName().split(".")[0]

fun String.getDirectory() = this
	.split('/', '\\')
	.filter { it.isNotBlank() }
	.dropLast(1)
	.joinToString("/")

fun pathCombine(vararg parts: String): String {
	val builder = StringBuilder()
	builder.append(parts[0])
	for (i in 1 until parts.size) {
		val part = parts[i]
		if (part.isBlank()) continue

		if (!builder.endsWith("/")) {
			builder.append("/")
		}

		builder.append(part)
	}

	return builder.toString().replace("\\", "/").replace("//", "/")
}

fun relPath(path: String, relativeTo: String): String
{
	val pathParts = path.split('/', '\\').filter { it.isNotEmpty() }
	val relToParts = relativeTo.split('/', '\\').filter { it.isNotEmpty() }

	val output = StringBuilder()

	val minSize = min(pathParts.size, relToParts.size)
	var i = 0
	for (j in 0 until minSize) {
		val pathPart = pathParts[i]
		val relToPart = relToParts[i]

		if (pathPart != relToPart) {
			break
		}

		i++
	}

	for (ri in i until relToParts.size) {
		if (output.isNotEmpty()) {
			output.append("/")
		}
		output.append("..")
	}
	for (pi in i until pathParts.size) {
		if (output.isNotEmpty()) {
			output.append("/")
		}
		output.append(pathParts[pi])
	}

	return output.toString()
}

fun String.parseCategorisedString(): HashMap<String, List<String>>
{
	val output = HashMap<String, List<String>>()

	if (this.contains('('))
	{
		val categories = this.split(')')
		for (category in categories)
		{
			if (category.isBlank()) continue

			val split = category.split('(')
			var name = split[0].trim()
			if (name.startsWith(',')) name = name.substring(1)
			val defs = split[1].split(',').map { it.trim() }

			output[name] = defs
		}
	}
	else
	{
		val defs = this.split(',').map { it.trim() }
		output[""] = defs
	}

	return output
}

fun String.hex2Rgb(): String {
	return this.substring(1, 3).toInt(16).toString() + "," +
			this.substring(3, 5).toInt(16).toString() + "," +
			this.substring(5, 7).toInt(16).toString()
}

fun componentToHex(c: Int): String {
	val hex = c.toString(16);
	return if (hex.length == 1) "0$hex" else hex
}

fun String.rgb2Hex(): String {
	val components = this.split(',')
	return "#" +
			componentToHex(components[0].toInt()) +
			componentToHex(components[1].toInt()) +
			componentToHex(components[2].toInt())
}

fun String.prepareStackForToast(): String {
	val lines = this.split('\n')
	val simpleLines = lines.map { it.split("@webpack")[0] }
	return simpleLines.joinToString("<br />")
}

fun generateUUID(): String {
	val template = "xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx"
	val xs = "0123456789abcdef"
	val ms = "12345"
	val ns = "89ab"

	val output = StringBuilder()

	for (char in template) {
		val values = when(char) {
			'x' -> xs
			'M' -> ms
			'N' -> ns
			else -> {
				""
			}
		}

		if (values.isBlank()) {
			output.append(char)
			continue
		}

		val c = values[Random.nextInt(values.length)]
		output.append(c)
	}

	return output.toString()
}

fun String.removeTags(): String {
	return this.replace(Regex("<[^>]*>"), "")
}