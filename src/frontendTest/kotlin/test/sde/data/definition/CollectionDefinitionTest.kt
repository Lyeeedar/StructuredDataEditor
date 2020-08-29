package test.sde.data.definition

import sde.data.DataDocument
import sde.data.Project
import sde.data.definition.*
import sde.data.item.BooleanItem
import sde.data.item.CollectionItem
import sde.data.item.NumberItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class CollectionDefinitionTest
{
	@Test
	fun testParse()
	{
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Collection">
				<Data Name="Count1" meta:RefKey="Number" />
				<Data Name="Count2" meta:RefKey="Number" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")
		def.resolve__test()

		assertTrue(def is CollectionDefinition)
		assertEquals(1, def.contents.size)
		assertEquals(2, def.contents[0].second.size)
		assertTrue(def.contents[0].second[0] is NumberDefinition)
		assertEquals("Count2", def.contents[0].second[1].name)
	}

	@Test
	fun testDefKey()
	{
		val xml = """
			<Definitions xmlns:meta="Editor">
				<Data Name="Block" meta:RefKey="Collection">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
				<Data Name="Child" DefKey="Block" meta:RefKey="Collection">
					<Data Name="Count2" meta:RefKey="Number" />
				</Data>
			</Definitions>
		""".trimIndent()

		val defMap = xml.parseProjectAndResolve()

		assertEquals(2, defMap.size)

		val blockDef = defMap["Block"]
		assertNotNull(blockDef)
		assertTrue(blockDef is CollectionDefinition)
		assertEquals(1, blockDef.contents.size)
		assertEquals(1, blockDef.contents[0].second.size)

		val childDef = defMap["Child"]
		assertNotNull(childDef)

		assertTrue(childDef is CollectionDefinition)
		assertEquals("Child", childDef.name)
		assertEquals(1, childDef.contents.size)
		assertEquals(2, childDef.contents[0].second.size)
		assertEquals("Count2", childDef.contents[0].second[0].name)
		assertEquals("Count1", childDef.contents[0].second[1].name)
	}

	@Test
	fun testLoad() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Collection">
				<Data Name="Num" meta:RefKey="Number" />
				<Data Name="IsAwesome" meta:RefKey="Boolean" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Block>
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
				<IsAwesome>false</IsAwesome>
				<Num>1</Num>
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")
		def.resolve__test()

		val dataDoc = DataDocument("")
		val data = dataDoc.loadItem(def, dataXml.root)

		assertTrue(data is CollectionItem)
		assertEquals("Block (4)", data.name)
		assertEquals(4, data.children.size)

		val child2 = data.children[1]
		assertTrue(child2 is BooleanItem)
		assertEquals("IsAwesome", child2.name)
		assertEquals(true, child2.value)
		assertEquals(true, child2.isCollectionChild)

		val child3 = data.children[2]
		assertTrue(child3 is BooleanItem)
		assertEquals("IsAwesome", child3.name)
		assertEquals(false, child3.value)
		assertEquals(true, child3.isCollectionChild)

		val child4 = data.children[3]
		assertTrue(child4 is NumberItem)
		assertEquals("Num", child4.name)
		assertEquals(1f, child4.value)
		assertEquals(true, child4.isCollectionChild)
	}

	@Test
	fun testSave() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Collection">
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
		def.resolve__test()

		val dataDoc = DataDocument("")
		val data = dataDoc.loadItem(def, dataXml.root)

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
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Collection">
				<Data Name="Num" meta:RefKey="Number" />
				<!-- comment -->
				<Data Name="IsAwesome" meta:RefKey="Boolean" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Block>
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
				<!-- real comment -->
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")
		def.resolve__test()

		assertTrue(def is CollectionDefinition)
		assertEquals(2, def.contents.size)
		assertEquals(1, def.contents[0].second.size)
		assertEquals(" comment ", def.contents[1].first)
		assertEquals(1, def.contents[1].second.size)

		val dataDoc = DataDocument("")
		val data = dataDoc.loadItem(def, dataXml.root)

		assertTrue(data is CollectionItem)
		assertEquals("Block (2)", data.name)
		assertEquals(3, data.children.size)

		assertEquals("""
			<Block>
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
				<!-- real comment -->
			</Block>
		""".trimIndent(), data.def.saveItem(data).toString())
	}

	@Test
	fun testAdditionalDefs()
	{
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="Collection">
				<AdditionalDefs>
					<Data Name="Inserted" meta:RefKey="String" />
				</AdditionalDefs>
				<Data Name="Count1" meta:RefKey="Number" />
				<Data Name="Count2" meta:RefKey="Number" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")
		def.resolve__test()

		assertTrue(def is CollectionDefinition)
		assertEquals(1, def.contents.size)
		assertEquals(2, def.contents[0].second.size)

		assertEquals(1, def.additionalDefs.size)
		assertTrue(def.additionalDefs[0] is StringDefinition)

		assertTrue(def.contents[0].second[0] is NumberDefinition)
		assertEquals("Count2", def.contents[0].second[1].name)

		val item = def.createItem(DataDocument(""))
		assertEquals(1, item.children.size)
		assertEquals("Inserted", item.children[0].def.name)
		assertEquals(false, item.children[0].isCollectionChild)
	}
}