package sde.data

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.DefinitionMap
import sde.project.ProjectDef
import sde.utils.elements
import sde.utils.getAttributeValue
import sde.utils.parseXml

class Project(val def: ProjectDef)
{
	companion object
	{
		fun parseDefinitionsFile(contents: String, srcFile: String): DefinitionMap
		{
			val xml = contents.parseXml()
			val root = xml.firstElementChild!!

			val fileColour = root.getAttributeValue("Colour", "")
			val fileIcon = root.getAttributeValue("Icon", "")

			val map = DefinitionMap()
			for (element in root.childNodes.elements())
			{
				val def = AbstractDataDefinition.load(element, srcFile)
				map[def.name] = def
			}

			return map
		}
	}
}