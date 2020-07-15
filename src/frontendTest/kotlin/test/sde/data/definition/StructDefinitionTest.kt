package test.sde.data.definition

import sde.data.definition.AbstractDataDefinition
import sde.data.definition.NumberDefinition
import sde.data.definition.StructDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StructDefinitionTest
{
	@Test
	fun testParse()
	{
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Struct">
				<Data Name="Count1" meta:RefKey="Number" />
				<Data Name="Count2" meta:RefKey="Number" />
			</Data>
		""".trimIndent()

		val def = AbstractDataDefinition.load(xml)

		assertTrue(def is StructDefinition)
		assertEquals(1, def.contents.size)
		assertEquals(2, def.contents[0].second.size)
		assertTrue(def.contents[0].second[0] is NumberDefinition)
		assertEquals("Count2", def.contents[0].second[1].name)
	}
}