package sde.data.definition

import org.w3c.dom.Node
import org.w3c.dom.XMLDocument
import sde.utils.getAttributeValue
import sde.utils.parseXml

abstract class AbstractDataDefinition
{
	lateinit var srcFile: String

	lateinit var name: String
	lateinit var textColour: String
	lateinit var toolTip: String
	lateinit var visibleIf: String
	var skipIfDefault: Boolean = false

	companion object
	{
		fun load(contents: String): AbstractDataDefinition
		{
			val xml = contents.parseXml()

			return load(xml.childNodes.item(0)!!)
		}

		fun load(xml: Node): AbstractDataDefinition
		{
			var type = xml.getAttributeValue("meta:RefKey", "").toUpperCase()
			if (type.endsWith("DEF")) {
				type = type.substring(0, type.length - "DEF".length)
			}

			val def = when(type)
			{
				"NUMBER" -> NumberDefinition()
				else -> throw Exception("Unknown definition type $type")
			}

			val name = xml.getAttributeValue("Name", "")

			return def
		}
	}
}