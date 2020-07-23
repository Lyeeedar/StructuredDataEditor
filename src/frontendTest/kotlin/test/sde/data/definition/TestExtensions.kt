package test.sde.data.definition

import sde.data.definition.DataDefinition
import sde.data.definition.DefinitionMap

fun DataDefinition.resolve__test() {
	val defMap = DefinitionMap()
	defMap[this.name] = this

	val allDefs = HashMap<String, DefinitionMap>()
	allDefs[""] = defMap

	this.resolve(DefinitionMap(), allDefs)
}