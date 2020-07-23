package sde.data.definition

import sde.data.Project

object CoreDefinitions
{
    private val coreXmlDef: String = pl.treksoft.kvision.require("defs/Core.xmldef") as String
    val coreDefinitions: DefinitionMap = Project.parseDefinitionsFile(coreXmlDef, "")
}