package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.BooleanDefinition
import sde.data.DataDocument
import sde.data.item.BooleanItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BooleanDefinitionTest
{
    @Test
    fun testParse()
    {
        val xml = """
			<Data Name="IsAwesome" Default="true" xmlns:meta="Editor" meta:RefKey="Boolean" />
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(xml, "")

        assertTrue(def is BooleanDefinition)
        assertEquals("IsAwesome", def.name)
        assertEquals(true, def.default.toBoolean())
    }

    @Test
    fun testLoad()
    {
        val defXml = """
			<Data Name="IsAwesome" xmlns:meta="Editor" meta:RefKey="Boolean" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<IsAwesome>true</IsAwesome>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")

        assertTrue(def is BooleanDefinition)

        val dataDoc = DataDocument("")
        val data = dataDoc.loadItem(def, dataXml.root)

        assertTrue(data is BooleanItem)
        assertEquals(true, data.value)
        assertFalse(data.isDefault())
    }

    @Test
    fun testSave()
    {
        val defXml = """
			<Data Name="IsAwesome" xmlns:meta="Editor" meta:RefKey="Boolean" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<IsAwesome>true</IsAwesome>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")

	    val dataDoc = DataDocument("")
	    val data = dataDoc.loadItem(def, dataXml.root)

        assertTrue(data is BooleanItem)

        assertEquals("<IsAwesome>true</IsAwesome>", data.def.saveItem(data).toString())
        data.value = false
        assertEquals("<IsAwesome>false</IsAwesome>", data.def.saveItem(data).toString())
    }
}