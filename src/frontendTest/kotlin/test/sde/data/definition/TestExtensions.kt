package test.sde.data.definition

import sde.data.Project
import sde.data.definition.DataDefinition
import sde.data.definition.DefinitionMap
import kotlin.test.assertEquals

fun DataDefinition.resolve__test() {
	val defMap = DefinitionMap()
	defMap[this.name] = this

	val allDefs = HashMap<String, DefinitionMap>()
	allDefs[""] = defMap

	this.resolve(DefinitionMap(), allDefs)
}

fun String.parseProjectAndResolve(): DefinitionMap
{
	val defMap = Project.parseDefinitionsFile(this, "")

	val maps = HashMap<String, DefinitionMap>()
	maps[""] = defMap

	for (def in defMap.values)
	{
		def.resolve(defMap, maps)
	}

	return defMap
}