package test.sde.data.definition

import sde.data.DataDocument
import sde.data.definition.CoreDefinitions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CoreDefinitionsTest
{
    @Test
    fun loadedTest() {
        assertEquals(30, CoreDefinitions.coreDefinitions.size)

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
}