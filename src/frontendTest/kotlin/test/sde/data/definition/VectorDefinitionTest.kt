package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.VectorDefinition
import sde.data.DataDocument
import sde.data.item.VectorItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VectorDefinitionTest
{
	@Test
	fun testParse()
	{
		val xml = """
			<Data Name="Count" NumComponents="4" Name4="Cheese" Min="2" Max="5" Default="3" Type="Int" xmlns:meta="Editor" meta:RefKey="Vector" />
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		assertTrue(def is VectorDefinition)
		assertEquals("Count", def.name)
		assertEquals(4, def.numComponents)
		assertEquals(2f, def.minValue)
		assertEquals(5f, def.maxValue)
		assertEquals(3f, def.vectorDefault[2])
		assertEquals(true, def.useIntegers)
		assertEquals("X", def.xName)
		assertEquals("Cheese", def.wName)
	}

	@Test
	fun testLoad()
	{
		val defXml = """
			<Data Name="Count" xmlns:meta="Editor" meta:RefKey="Vector" />
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Count>10,15</Count>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(defXml, "")

		assertTrue(def is VectorDefinition)

		val dataDoc = DataDocument("")
		val data = dataDoc.loadItem(def, dataXml.root)

		assertTrue(data is VectorItem)
		assertEquals(10.0f, data.value1)
		assertEquals(15.0f, data.value2)
		assertEquals(0.0f, data.value3)
		assertFalse(data.isDefault())
	}

	@Test
	fun testSave()
	{
		val defXml = """
			<Data Name="Count" xmlns:meta="Editor" meta:RefKey="Vector" />
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Count>10,15</Count>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(defXml, "")

		val dataDoc = DataDocument("")
		val data = dataDoc.loadItem(def, dataXml.root)

		assertTrue(data is VectorItem)

		assertEquals("<Count>10,15</Count>", data.def.saveItem(data).toString())
		data.value1 = 5.5f
		assertEquals("<Count>5.5,15</Count>", data.def.saveItem(data).toString())
	}
}