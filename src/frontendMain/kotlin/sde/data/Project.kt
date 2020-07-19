package sde.data

import kotlinx.coroutines.launch
import sde.Services
import sde.data.definition.AbstractDataDefinition
import sde.data.definition.DataDefinition
import sde.data.definition.DefinitionMap
import sde.project.ProjectDef
import sde.project.ProjectExplorerPage
import sde.util.XElement
import sde.utils.DefinitionLoadException
import sde.utils.parseXml
import sde.utils.toXDocument

class Project(val def: ProjectDef, val page: ProjectExplorerPage)
{
	var recentItems: List<String> = ArrayList()

	val globalDefs = HashMap<String, DataDefinition>()
	val definitions = HashMap<String, DefinitionMap>()
	val rootDefinitions = HashMap<String, DataDefinition>()

	init {
		page.scope.launch {
			loadDefinitions(def.defsFolder)
			for (defMap in definitions.values) {
				for (def in defMap.values) {
					def.resolve(globalDefs, definitions)
				}
			}
		}
	}

	private suspend fun loadDefinitions(folder: String) {
		val contents = Services.disk.getFolderContents(folder)

		for (item in contents) {
			if (item.isDirectory) {
				loadDefinitions(item.path)
			} else if (item.path.endsWith(".xmldef")) {
				val contents = Services.disk.loadFileString(item.path)
				val defMap = parseDefinitionsFile(contents, item.path)

				definitions[item.path] = defMap

				for (def in defMap.values) {
					if (def.isGlobal || !def.isDef) {
						if (globalDefs.containsKey(def.name)) {
							throw DefinitionLoadException("Duplicate definition with name ${def.name}")
						}

						globalDefs[def.name] = def
					}
					if (!def.isDef) {
						rootDefinitions[def.name] = def
					}
				}
			}
		}
	}

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
				def.fileColour = fileColour
				def.fileIcon = fileIcon

				map[def.name] = def
			}

			return map
		}
	}
}