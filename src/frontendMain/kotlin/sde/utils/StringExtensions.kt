package sde.utils

fun String.getFileName(): String = this.split('/', '\\').last()

fun String.parseCategoriedString(): HashMap<String, List<String>>
{
	val output = HashMap<String, List<String>>()

	if (this.contains('('))
	{
		val categories = this.split(')')
		for (category in categories)
		{
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