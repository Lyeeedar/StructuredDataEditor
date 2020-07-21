package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.StringDefinition
import sde.data.DataDocument
import sde.data.item.StringItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringDefinitionTest
{
    @Test
    fun testParse()
    {
        val xml = """
			<Data Name="IsAwesome" Default="true" xmlns:meta="Editor" meta:RefKey="String" />
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(xml, "")

        assertTrue(def is StringDefinition)
        assertEquals("IsAwesome", def.name)
        assertEquals("true", def.default)
    }

    @Test
    fun testLoad()
    {
        val defXml = """
			<Data Name="IsAwesome" xmlns:meta="Editor" meta:RefKey="String" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<IsAwesome>true</IsAwesome>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")

        assertTrue(def is StringDefinition)

        val dataDoc = DataDocument("")
        val data = def.loadItem(dataDoc, dataXml.root)

        assertTrue(data is StringItem)
        assertEquals("true", data.value)
        assertFalse(data.isDefault())
    }

    @Test
    fun testSave()
    {
        val defXml = """
			<Data Name="IsAwesome" xmlns:meta="Editor" meta:RefKey="String" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<IsAwesome>true</IsAwesome>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")
        val data = def.loadItem(DataDocument(""), dataXml.root)

        assertTrue(data is StringItem)

        assertEquals("<IsAwesome>true</IsAwesome>", data.def.saveItem(data).toString())
        data.value = "false"
        assertEquals("<IsAwesome>false</IsAwesome>", data.def.saveItem(data).toString())
    }
}