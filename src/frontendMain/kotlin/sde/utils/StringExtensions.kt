package sde.utils


fun String.getFileName(): String = this.split('/', '\\').last()

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