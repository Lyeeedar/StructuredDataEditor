package test.sde.data.definition

import sde.data.Project
import sde.data.definition.AbstractDataDefinition
import sde.data.definition.DefinitionMap
import sde.data.DataDocument
import sde.data.definition.NumberDefinition
import sde.data.definition.StructDefinition
import sde.data.item.StructItem
import sde.data.item.NumberItem
import sde.data.item.BooleanItem
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

		val defMap = xml.parseProjectAndResolve()

		assertEquals(2, defMap.size)

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

	@Test
	fun testLoad() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Struct">
				<Data Name="Num" meta:RefKey="Number" />
				<Data Name="IsAwesome" meta:RefKey="Boolean" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Block>
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertTrue(data is StructItem)
		assertEquals("Block", data.name)
		assertEquals(2, data.children.size)

		val child2 = data.children[1]
		assertTrue(child2 is BooleanItem)
		assertEquals("IsAwesome", child2.name)
		assertEquals(true, child2.value)
	}

	@Test
	fun testSave() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Struct">
				<Data Name="Num" meta:RefKey="Number" />
				<Data Name="IsAwesome" meta:RefKey="Boolean" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Block>
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertEquals("""
			<Block>
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent(), data.def.saveItem(data).toString())
	}

	@Test
	fun testComments() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Struct">
				<Data Name="Num" meta:RefKey="Number" />
				<!-- comment -->
				<Data Name="IsAwesome" meta:RefKey="Boolean" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Block>
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		assertTrue(def is StructDefinition)
		assertEquals(2, def.contents.size)
		assertEquals(1, def.contents[0].second.size)
		assertEquals(" comment ", def.contents[1].first)
		assertEquals(1, def.contents[1].second.size)

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertTrue(data is StructItem)
		assertEquals("Block", data.name)
		assertEquals(3, data.children.size)

		assertEquals("""
			<Block>
				<Num>4</Num>
				<!-- comment -->
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent(), data.def.saveItem(data).toString())
	}
}