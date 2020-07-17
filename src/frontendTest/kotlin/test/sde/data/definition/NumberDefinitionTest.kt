package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.NumberDefinition
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
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
}