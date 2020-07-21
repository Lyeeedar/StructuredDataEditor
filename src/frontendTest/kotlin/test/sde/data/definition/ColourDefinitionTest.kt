package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.ColourDefinition
import sde.data.DataDocument
import sde.data.item.ColourItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColourDefinitionTest
{
    @Test
    fun testParse()
    {
        val xml = """
			<Data Name="Tint" Default="255,255,255,255" xmlns:meta="Editor" meta:RefKey="Colour" />
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(xml, "")

        assertTrue(def is ColourDefinition)
        assertEquals("Tint", def.name)
        assertEquals("255,255,255,255", def.default)
    }

    @Test
    fun testLoad()
    {
        val defXml = """
			<Data Name="Tint" xmlns:meta="Editor" meta:RefKey="Colour" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<Tint>125,178,100</Tint>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")

        assertTrue(def is ColourDefinition)

        val dataDoc = DataDocument("")
        val data = def.loadItem(dataDoc, dataXml.root)

        assertTrue(data is ColourItem)
        assertEquals("125,178,100", data.value)
        assertFalse(data.isDefault())
    }

    @Test
    fun testSave()
    {
        val defXml = """
			<Data Name="Tint" xmlns:meta="Editor" meta:RefKey="Colour" />
		""".trimIndent().parseXml().toXDocument()

        val dataXml = """
			<Tint>125,178,100</Tint>
		""".trimIndent().parseXml().toXDocument()

        val def = AbstractDataDefinition.load(defXml, "")
        val data = def.loadItem(DataDocument(""), dataXml.root)

        assertTrue(data is ColourItem)

        assertEquals("<Tint>125,178,100</Tint>", data.def.saveItem(data).toString())
        data.value = "255,0,0,255"
        assertEquals("<Tint>255,0,0,255</Tint>", data.def.saveItem(data).toString())
    }
}