package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.EnumDefinition
import sde.data.DataDocument
import sde.data.item.EnumItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnumDefinitionTest
{
    @Test
    fun testParse()
    {
        val xml = """
			<Data Name="IsAwesome" EnumValues="Yes,No" Default="No" xmlns:meta="Editor" meta:RefKey="Enum" />
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(xml, "")

        assertTrue(def is EnumDefinition)
        assertEquals("IsAwesome", def.name)
        assertEquals("No", def.default)
    }

    @Test
    fun testLoad()
    {
        val defXml = """
			<Data Name="IsAwesome" EnumValues="Yes,No" xmlns:meta="Editor" meta:RefKey="Enum" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<IsAwesome>No</IsAwesome>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")

        assertTrue(def is EnumDefinition)

        val dataDoc = DataDocument("")
        val data = def.loadItem(dataDoc, dataXml.root)

        assertTrue(data is EnumItem)
        assertEquals("No", data.value)
        assertFalse(data.isDefault())
    }

    @Test
    fun testSave()
    {
        val defXml = """
			<Data Name="IsAwesome" EnumValues="Yes,No" xmlns:meta="Editor" meta:RefKey="Enum" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<IsAwesome>Yes</IsAwesome>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")
        val data = def.loadItem(DataDocument(""), dataXml.root)

        assertTrue(data is EnumItem)

        assertEquals("<IsAwesome>Yes</IsAwesome>", data.def.saveItem(data).toString())
        data.value = "No"
        assertEquals("<IsAwesome>No</IsAwesome>", data.def.saveItem(data).toString())
    }
}