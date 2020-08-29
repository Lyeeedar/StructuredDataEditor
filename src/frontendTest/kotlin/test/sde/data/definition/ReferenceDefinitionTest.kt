package test.sde.data.definition

import sde.data.Project
import sde.data.DataDocument
import sde.data.definition.*
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
	fun testParse2()
	{
		val xml = """
<Definitions xmlns:meta="Editor">
	<Definition Name="Wait" Nullable="False" TextColour="206,206,2" meta:RefKey="StructDef">
		<Const Name="classID">Wait</Const>
		<Data Name="Count" SkipIfDefault="False" Default="1" meta:RefKey="String" />
	</Definition>
	
	<Definition Name="AbstractBehaviourActionDefs" Keys="Wait" meta:RefKey="ReferenceDef" />
	<Definition Name="AbstractBehaviourNodeDefs" Keys="Other(RunAll,RunOneRandomly),Until(RunUntilNotCompleted,RunUntilNotFailed,RunUntilState)" meta:RefKey="ReferenceDef" />
	
	<Definition Name="RunAll" DefKey="AbstractBehaviourActionDefs" Background="204,28,28" meta:RefKey="GraphCollectionDef">
	</Definition>
	<Definition Name="RunOneRandomly" DefKey="AbstractBehaviourActionDefs" Background="26,204,26" meta:RefKey="GraphCollectionDef">
	</Definition>
	<Definition Name="RunUntilNotCompleted" DefKey="AbstractBehaviourActionDefs" Background="49,49,204" meta:RefKey="GraphCollectionDef">
	</Definition>
	<Definition Name="RunUntilNotFailed" DefKey="AbstractBehaviourActionDefs" Background="53,204,53" meta:RefKey="GraphCollectionDef">
	</Definition>
	<Definition Name="RunUntilState" DefKey="AbstractBehaviourActionDefs" Background="78,204,204" HasAttributes="True" meta:RefKey="GraphCollectionDef">
		<Attributes meta:RefKey="Attributes">
			<Data Name="TargetState" EnumValues="Completed,Running,Failed" SkipIfDefault="False" meta:RefKey="Enum" />
		</Attributes>
	</Definition>
</Definitions>
		""".trimIndent()

		val defs = xml.parseProjectAndResolve()
		assertEquals(8, defs.size)

		val def = defs["AbstractBehaviourNodeDefs"]
		assertTrue(def is ReferenceDefinition)

		assertEquals(5, def.referenceMap.size)

		assertEquals(3, def.contents.size)

		assertEquals(2, def.contents[1].second.size)
		assertEquals("Other", def.contents[1].first)

		assertEquals(3, def.contents[2].second.size)
		assertEquals("Until", def.contents[2].first)

		assertEquals(5, def.contentsMap.size)
		assertTrue(def.contents[1].second[0] is GraphCollectionDefinition)
		assertEquals("RunOneRandomly", def.contents[1].second[1].name)
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
		val data = dataDoc.loadItem(def, dataXml.root)

		assertTrue(data is ReferenceItem)
		assertEquals("Ref (Block2)", data.name)
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