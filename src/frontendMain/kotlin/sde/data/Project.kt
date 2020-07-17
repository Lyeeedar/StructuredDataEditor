package sde.data

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.DefinitionMap
import sde.project.ProjectDef
import sde.util.XElement
import sde.utils.parseXml
import sde.utils.toXDocument

class Project(val def: ProjectDef)
{
	companion object
	{
		fun parseDefinitionsFile(contents: String, srcFile: String): DefinitionMap
		{
			val xml = contents.parseXml().toXDocument()
			val root = xml.root

			val fileColour = root.getAttributeValue("Colour", "")
			val fileIcon = root.getAttributeValue("Icon", "")

			val map = DefinitionMap()
			for (element in root.children)
			{
				if (element !is XElement) continue

				val def = AbstractDataDefinition.load(element, srcFile)
				map[def.name] = def
			}

			return map
		}
	}
}