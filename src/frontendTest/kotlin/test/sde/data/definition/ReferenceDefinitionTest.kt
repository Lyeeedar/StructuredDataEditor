package test.sde.data.definition

import sde.data.Project
import sde.data.definition.AbstractDataDefinition
import sde.data.definition.DefinitionMap
import sde.data.DataDocument
import sde.data.definition.NumberDefinition
import sde.data.definition.ReferenceDefinition
import sde.data.item.ReferenceItem
import sde.data.item.NumberItem
import sde.data.item.BooleanItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReferenceDefinitionTest
{
	@Test
	fun testParse()
	{
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Reference">
				<Data Name="Count1" meta:RefKey="Number" />
				<Data Name="Count2" meta:RefKey="Number" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		assertTrue(def is ReferenceDefinition)
		assertEquals(1, def.contents.size)
		assertEquals(2, def.contents[0].second.size)
		assertTrue(def.contents[0].second[0] is NumberDefinition)
		assertEquals("Count2", def.contents[0].second[1].name)
	}

	@Test
	fun testKeys()
	{
		val xml = """
			<Definitions xmlns:meta="Editor">
				<Data Name="Block" meta:RefKey="Struct">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
				<Data Name="Ref" Keys="Block,Block2" meta:RefKey="Reference" />
				<Data Name="Block2" meta:RefKey="Struct">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
			</Definitions>
		""".trimIndent()

		val defMap = xml.parseProjectAndResolve()

		assertEquals(3, defMap.size)

		val refDef = defMap["Ref"]
		assertNotNull(refDef)

		assertTrue(refDef is ReferenceDefinition)
		assertEquals("Ref", refDef.name)
		assertEquals(1, refDef.contents.size)
		assertEquals(2, refDef.contents[0].second.size)
		assertEquals("Block", refDef.contents[0].second[0].name)
		assertEquals("Block2", refDef.contents[0].second[1].name)
	}

	@Test
	fun testLoad() {
		val xml = """
			<Definitions xmlns:meta="Editor">
				<Data Name="Block" meta:RefKey="Struct">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
				<Data Name="Ref" Keys="Block,Block2" meta:RefKey="Reference" />
				<Data Name="Block2" Extends="Block" meta:RefKey="Struct">
					<Data Name="Count2" meta:RefKey="Number" />
				</Data>
			</Definitions>
		""".trimIndent()

		val defMap = xml.parseProjectAndResolve()

		val dataXml = """
			<Ref meta:RefKey="Block2" xmlns:meta="Editor">
				<Count2>7</Count2>
				<Count1>4</Count1>
			</Ref>
		""".trimIndent().parseXml().toXDocument()

		val def = defMap["Ref"]
		assertNotNull(def)

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertTrue(data is ReferenceItem)
		assertEquals("Ref", data.name)
		assertEquals(2, data.children.size)
		assertNotNull(data.createdItem)
		assertEquals("Block2", data.createdItem!!.def.name)

		val child1 = data.children[0]
		assertTrue(child1 is NumberItem)
		assertEquals("Count2", child1.name)
		assertEquals(7f, child1.value)

		val child2 = data.children[1]
		assertTrue(child2 is NumberItem)
		assertEquals("Count1", child2.name)
		assertEquals(4f, child2.value)
	}

	@Test
	fun testSave() {
		val xml = """
			<Definitions xmlns:meta="Editor">
				<Data Name="Block" meta:RefKey="Struct">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
				<Data Name="Ref" Keys="Block,Block2" meta:RefKey="Reference" />
				<Data Name="Block2" meta:RefKey="Struct">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
			</Definitions>
		""".trimIndent()

		val defMap = xml.parseProjectAndResolve()

		val def = defMap["Ref"]
		assertNotNull(def)
		assertTrue(def is ReferenceDefinition)

		val item = def.createItem(DataDocument(""))
		item.create()

		assertEquals("""
			<Ref meta:RefKey="Block">
				<Count1>0</Count1>
			</Ref>
		""".trimIndent(), item.def.saveItem(item).toString())
	}
}