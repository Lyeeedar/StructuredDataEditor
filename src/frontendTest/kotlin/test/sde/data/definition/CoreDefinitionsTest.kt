package test.sde.data.definition

import sde.data.DataDocument
import sde.data.Project
import sde.data.definition.CoreDefinitions
import sde.data.definition.DefinitionMap
import sde.data.definition.StructDefinition
import sde.data.item.StructItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CoreDefinitionsTest
{
    @Test
    fun loadedTest() {
        assertEquals(29, CoreDefinitions.coreDefinitions.size)

        assertNotNull(CoreDefinitions.coreDefinitions.containsKey("Struct"))
        assertNotNull(CoreDefinitions.coreDefinitions.containsKey("StructDef"))

        assertNotNull(CoreDefinitions.coreDefinitions.containsKey("Collection"))
        assertNotNull(CoreDefinitions.coreDefinitions.containsKey("CollectionDef"))
    }

    @Test
    fun createTest() {
        assertTrue(CoreDefinitions.rootDef.isResolved)
        val item = CoreDefinitions.rootDef.createItem(DataDocument(""))
        assertNotNull(item)
    }

	@Test
	fun sandbox() {
		val xml = pl.treksoft.kvision.require("test/TestProject/Definitions/Sandbox.xmldef") as String
		val auxXml = pl.treksoft.kvision.require("test/TestProject/Definitions/AuxFiles.xmldef") as String

		val maps = HashMap<String, DefinitionMap>()
		maps["Sandbox"] = Project.parseDefinitionsFile(xml, "Sandbox")
		maps["AuxFiles"] = Project.parseDefinitionsFile(auxXml, "AuxFiles")

		val global = DefinitionMap()
		for (defMap in maps.values)
		{
			for (def in defMap.values)
			{
				if (def.isGlobal || !def.isDef) {
					global[def.name] = def
				}
			}
		}

		for (defMap in maps.values)
		{
			for (def in defMap.values)
			{
				def.resolve(global, maps)
			}
		}

		val def = maps["Sandbox"]!!["Sandbox"]
		assertNotNull(def)
		assertTrue(def is StructDefinition)

		val doc = DataDocument("")
		val item = def.createItem(doc)
		item.createContents()
		assertEquals(22, item.children.size)
	}
}