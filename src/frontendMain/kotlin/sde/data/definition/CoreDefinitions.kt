package sde.data.definition

import sde.data.Project

object CoreDefinitions
{
    private val coreXmlDef: String = pl.treksoft.kvision.require("defs/Core.xmldef") as String
    val coreDefinitions: DefinitionMap = Project.parseDefinitionsFile(coreXmlDef, "")
	val rootDef = coreDefinitions["Definitions"]!!

    init {
        val allDefs = HashMap<String, DefinitionMap>()
        allDefs[""] = coreDefinitions

        for (def in coreDefinitions.values) {
            def.resolve(coreDefinitions, allDefs)
        }
    }
}