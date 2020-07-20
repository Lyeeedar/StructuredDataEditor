package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.NumberDefinition
import sde.data.DataDocument
import sde.data.item.NumberItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumberDefinitionTest
{
	@Test
	fun testParse()
	{
		val xml = """
			<Data Name="Count" Min="2" Max="5" Default="3" Type="Int" xmlns:meta="Editor" meta:RefKey="Number" />
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		assertTrue(def is NumberDefinition)
		assertEquals("Count", def.name)
		assertEquals(2f, def.minValue)
		assertEquals(5f, def.maxValue)
		assertEquals(3f, def.default.toFloat())
		assertEquals(true, def.useIntegers)
	}

	@Test
	fun testLoad()
	{
		val defXml = """
			<Data Name="Count" xmlns:meta="Editor" meta:RefKey="Number" />
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Count>10</Count>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(defXml, "")

		assertTrue(def is NumberDefinition)

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertTrue(data is NumberItem)
		assertEquals(10.0f, data.value)
		assertFalse(data.isDefault())
	}

	@Test
	fun testSave()
	{
		val defXml = """
			<Data Name="Count" xmlns:meta="Editor" meta:RefKey="Number" />
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Count>10</Count>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(defXml, "")
		val data = def.loadItem(DataDocument(""), dataXml.root)

		assertTrue(data is NumberItem)

		assertEquals("<Count>10</Count>", data.def.saveItem(data).toString())
		data.value = 5.5f
		assertEquals("<Count>5.5</Count>", data.def.saveItem(data).toString())
	}
}