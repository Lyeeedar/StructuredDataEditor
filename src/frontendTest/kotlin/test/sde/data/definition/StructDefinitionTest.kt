package test.sde.data.definition

import sde.data.Project
import sde.data.definition.AbstractDataDefinition
import sde.data.definition.DefinitionMap
import sde.data.definition.NumberDefinition
import sde.data.definition.StructDefinition
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		assertTrue(def is StructDefinition)
		assertEquals(1, def.contents.size)
		assertEquals(2, def.contents[0].second.size)
		assertTrue(def.contents[0].second[0] is NumberDefinition)
		assertEquals("Count2", def.contents[0].second[1].name)
	}

	@Test
	fun testExtends()
	{
		val xml = """
			<Definitions xmlns:meta="Editor">
				<Data Name="Block" meta:RefKey="Struct">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
				<Data Name="Child" Extends="Block" meta:RefKey="Struct">
					<Data Name="Count2" meta:RefKey="Number" />
				</Data>
			</Definitions>
		""".trimIndent()

		val defMap = Project.parseDefinitionsFile(xml, "")

		assertEquals(2, defMap.size)

		val maps = HashMap<String, DefinitionMap>()
		maps[""] = defMap

		for (def in defMap.values)
		{
			def.resolve(defMap, maps)
		}

		val blockDef = defMap["Block"]
		assertNotNull(blockDef)
		assertTrue(blockDef is StructDefinition)
		assertEquals(1, blockDef.contents.size)
		assertEquals(1, blockDef.contents[0].second.size)

		val childDef = defMap["Child"]
		assertNotNull(childDef)

		assertTrue(childDef is StructDefinition)
		assertEquals("Child", childDef.name)
		assertEquals(1, childDef.contents.size)
		assertEquals(2, childDef.contents[0].second.size)
		assertEquals("Count2", childDef.contents[0].second[0].name)
		assertEquals("Count1", childDef.contents[0].second[1].name)
	}
}