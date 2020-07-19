package sde.data

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.treksoft.kvision.toast.Toast
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
	val projectRootFolder = def.projectRootPath.replace('\\', '/').replace("/ProjectRoot.xml", "")

	var recentItems: List<String> = ArrayList()

	val globalDefs = HashMap<String, DataDefinition>()
	val definitions = HashMap<String, DefinitionMap>()
	val rootDefinitions = HashMap<String, DataDefinition>()

	val definitionLoadErrors = ArrayList<Pair<String, String>>()

	var loadJob: Job? = null

	init {
		loadJob = page.launch {
			try {
				loadDefinitions(projectRootFolder + "/" + def.defsFolder)
				for (defMap in definitions.values) {
					for (def in defMap.values) {
						try {
							def.resolve(globalDefs, definitions)
						} catch (ex: Throwable) {
							definitionLoadErrors.add(Pair(def.srcFile, ex.message ?: ""))
						}
					}
				}

				Toast.success("Loaded ${rootDefinitions.size} resource types", "Definition load complete")
			} catch (ex: Throwable) {
				Toast.error(ex.message ?: ex.toString(), "Definition load failed")
			}

			loadJob = null
		}
	}

	private suspend fun loadDefinitions(folder: String) {
		val contents = Services.disk.getFolderContents(folder)

		for (item in contents) {
			if (item.isDirectory) {
				loadDefinitions(item.path)
			} else if (item.path.endsWith(".xmldef")) {
				try {
					val fileContents = Services.disk.loadFileString(item.path)
					val defMap = parseDefinitionsFile(fileContents, item.path)

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
				} catch (ex: Throwable) {
					definitionLoadErrors.add(Pair(item.path, ex.message ?: ""))
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