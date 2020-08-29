package test.sde.data.definition

import sde.data.DataDocument
import sde.data.Project
import sde.data.definition.*
import sde.data.item.BooleanItem
import sde.data.item.GraphStructItem
import sde.data.item.StructItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GraphStructDefinitionTest
{
	// basic struct tests
	@Test
	fun testParse()
	{
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="GraphStruct">
				<Data Name="Count1" meta:RefKey="Number" />
				<Data Name="Count2" meta:RefKey="Number" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		assertTrue(def is GraphStructDefinition)
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
				<Data Name="Block" meta:RefKey="GraphStruct">
					<Data Name="Count1" meta:RefKey="Number" />
				</Data>
				<Data Name="Child" Extends="Block" meta:RefKey="GraphStruct">
					<Data Name="Count2" meta:RefKey="Number" />
				</Data>
			</Definitions>
		""".trimIndent()

		val defMap = xml.parseProjectAndResolve()

		assertEquals(2, defMap.size)

		val blockDef = defMap["Block"]
		assertNotNull(blockDef)
		assertTrue(blockDef is GraphStructDefinition)
		assertEquals(1, blockDef.contents.size)
		assertEquals(1, blockDef.contents[0].second.size)

		val childDef = defMap["Child"]
		assertNotNull(childDef)

		assertTrue(childDef is GraphStructDefinition)
		assertEquals("Child", childDef.name)
		assertEquals(1, childDef.contents.size)
		assertEquals(2, childDef.contents[0].second.size)
		assertEquals("Count2", childDef.contents[0].second[0].name)
		assertEquals("Count1", childDef.contents[0].second[1].name)
	}

	@Test
	fun testLoad() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="GraphStruct">
				<Data Name="Num" meta:RefKey="Number" />
				<Data Name="IsAwesome" meta:RefKey="Boolean" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Block meta:X="240" meta:Y="0" GUID="mahguid" xmlns:meta="Editor">
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertTrue(data is GraphStructItem)
		assertEquals("Block", data.name)
		assertEquals(2, data.children.size)
		assertEquals(240.0, data.nodePositionX)
		assertEquals(0.0, data.nodePositionY)
		assertEquals("mahguid", data.guid)

		val child2 = data.children[1]
		assertTrue(child2 is BooleanItem)
		assertEquals("IsAwesome", child2.name)
		assertEquals(true, child2.value)
	}

	@Test
	fun testSave() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="GraphStruct">
				<Data Name="Num" meta:RefKey="Number" />
				<Data Name="IsAwesome" meta:RefKey="Boolean" />
			</Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Block meta:X="240" meta:Y="0" GUID="mahguid" xmlns:meta="Editor">
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertEquals("""
			<Block meta:X="240" meta:Y="0" GUID="mahguid">
				<Num>4</Num>
				<IsAwesome>true</IsAwesome>
			</Block>
		""".trimIndent(), data.def.saveItem(data).toString())
	}

	@Test
	fun testComments() {
		val xml = """
			<Data Name="Block" xmlns:meta="Editor" meta:RefKey="GraphStruct">
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

		assertTrue(def is GraphStructDefinition)
		assertEquals(2, def.contents.size)
		assertEquals(1, def.contents[0].second.size)
		assertEquals(" comment ", def.contents[1].first)
		assertEquals(1, def.contents[1].second.size)

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertTrue(data is GraphStructItem)
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

	// flat data tests
	fun getDefFile(): String {
		val def: String = """
			<Definitions Colour="121,252,218" Icon="Sprites/Icons/CardIcon.png" xmlns:meta="Editor">
				<Definition Name="BehaviourTree" AllowCircularLinks="True" FlattenData="True" NodeStoreName="NodeMap" Nullable="False" TextColour="14,204,14" meta:RefKey="GraphStruct">
					<Data Name="Root" DefKey="AbstractBehaviourNodeDefs" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="AbstractBehaviourAction" Nullable="False" TextColour="111,204,111" Extends="AbstractBehaviourTreeItem" meta:RefKey="StructDef">
				</Definition>
				<Definition Name="AbstractBehaviourActionDefs" Keys="(UseAbility),Action(Attack,Kill,MoveTo,ProcessInput,Wait),Data(ClearValue,ConvertToPosition,Datascope,PickOneFrom,SetValue),Debug(Breakpoint),Entity(GetAgro,GetAllVisible),Flow Control(Branch,Import,Node,SetState)" meta:RefKey="ReferenceDef" />
				<Definition Name="AbstractBehaviourNodeDefs" Keys="Other(RunAll,RunOneRandomly),Until(RunUntilNotCompleted,RunUntilNotFailed,RunUntilState)" meta:RefKey="ReferenceDef" />
				<Definition Name="AbstractBehaviourTreeItem" Nullable="False" TextColour="210,210,100" meta:RefKey="StructDef">
				</Definition>
				<Definition Name="AbstractBehaviourTreeItemDefs" Keys="(UseAbility),Action(Attack,Kill,MoveTo,ProcessInput,Wait),Data(ClearValue,ConvertToPosition,Datascope,PickOneFrom,SetValue),Debug(Breakpoint),Entity(GetAgro,GetAllVisible),Flow Control(Branch,Import,Node,SetState),Other(RunAll,RunOneRandomly),Until(RunUntilNotCompleted,RunUntilNotFailed,RunUntilState)" meta:RefKey="ReferenceDef" />
				<Definition Name="Attack" Nullable="False" TextColour="19,214,214" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Attack</Const>
					<Data Name="Key" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="Branch" Nullable="False" TextColour="238,94,94" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Branch</Const>
					<Data Name="Branches" Keys="ConditionAndNode" meta:RefKey="Collection" />
				</Definition>
				<Definition Name="Breakpoint" Nullable="False" TextColour="67,204,67" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Breakpoint</Const>
					<Data Name="Context" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="ClearValue" Nullable="False" TextColour="250,250,39" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">ClearValue</Const>
					<Data Name="Key" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="ConditionAndNode" Nullable="False" TextColour="204,204,4" meta:RefKey="StructDef">
					<Data Name="Condition" SkipIfDefault="False" Default="1" ToolTip="Known variables: else" meta:RefKey="String" />
					<Data Name="Node" DefKey="AbstractBehaviourNodeDefs" UseParentDescription="True" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="ConvertToPosition" Nullable="False" TextColour="132,204,204" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">ConvertToPosition</Const>
					<Data Name="Input" SkipIfDefault="False" Default="" meta:RefKey="String" />
					<Data Name="Output" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="Datascope" Nullable="False" TextColour="241,17,17" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Datascope</Const>
					<Data Name="Node" DefKey="AbstractBehaviourNodeDefs" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="GetAgro" Nullable="False" TextColour="210,210,123" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">GetAgro</Const>
					<Data Name="Key" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="GetAllVisible" Nullable="False" TextColour="204,127,127" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">GetAllVisible</Const>
					<Data Name="Type" EnumValues="Tiles,Allies,Enemies" SkipIfDefault="False" meta:RefKey="Enum" />
					<Data Name="Key" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="Import" Nullable="False" TextColour="244,117,244" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Import</Const>
					<Data Name="Path" StripExtension="True" ResourceType="BehaviourTree" SkipIfDefault="False" Default="" meta:RefKey="File" />
				</Definition>
				<Definition Name="Kill" Nullable="False" TextColour="17,17,204" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Kill</Const>
				</Definition>
				<Definition Name="MoveTo" Nullable="False" TextColour="228,18,228" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">MoveTo</Const>
					<Data Name="Dst" Type="Int" Default="0" SkipIfDefault="True" meta:RefKey="Number" />
					<Data Name="Towards" SkipIfDefault="True" Default="true" meta:RefKey="Boolean" />
					<Data Name="Key" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="Node" Nullable="False" TextColour="26,204,204" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Node</Const>
					<Data Name="Node" DefKey="AbstractBehaviourNodeDefs" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="PickOneFrom" Nullable="False" TextColour="15,218,15" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">PickOneFrom</Const>
					<Data Name="Input" SkipIfDefault="False" Default="" meta:RefKey="String" />
					<Data Name="Output" SkipIfDefault="False" Default="" meta:RefKey="String" />
					<Data Name="Condition" SkipIfDefault="False" Default="dist" ToolTip="Known variables: dist,hp,level,damage,random" meta:RefKey="String" />
					<Data Name="Minimum" SkipIfDefault="True" Default="true" meta:RefKey="Boolean" />
				</Definition>
				<Definition Name="ProcessInput" Nullable="False" TextColour="253,253,85" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">ProcessInput</Const>
				</Definition>
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
				<Definition Name="SetState" Nullable="False" TextColour="20,20,204" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">SetState</Const>
					<Data Name="OutputMap" Nullable="False" SkipIfDefault="False" meta:RefKey="Struct">
						<Data Name="Completed" EnumValues="Completed,Running,Failed" meta:RefKey="Enum" />
						<Data Name="Running" EnumValues="Completed,Running,Failed" meta:RefKey="Enum" />
						<Data Name="Failed" EnumValues="Completed,Running,Failed" meta:RefKey="Enum" />
					</Data>
					<Data Name="Node" DefKey="AbstractBehaviourNodeDefs" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="SetValue" Nullable="False" TextColour="22,204,22" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">SetValue</Const>
					<Data Name="Key" SkipIfDefault="False" Default="" meta:RefKey="String" />
					<Data Name="Value" SkipIfDefault="False" Default="1" meta:RefKey="String" />
				</Definition>
				<Definition Name="UseAbility" Nullable="False" TextColour="82,82,204" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">UseAbility</Const>
					<Data Name="Key" SkipIfDefault="False" Default="" meta:RefKey="String" />
				</Definition>
				<Definition Name="Wait" Nullable="False" TextColour="206,206,2" Extends="AbstractBehaviourAction" meta:RefKey="StructDef">
					<Const Name="classID">Wait</Const>
					<Data Name="Count" SkipIfDefault="False" Default="1" meta:RefKey="String" />
				</Definition>
			</Definitions>
		""".trimIndent()
		assertNotNull(def)
		assertTrue { def.contains("Name=\"BehaviourTree\" ") }

		return def
	}

	@Test
	fun parseLarge() {
		val defRaw = getDefFile()
		val parsed = defRaw.parseProjectAndResolve()

		assertEquals(30, parsed.size)
		assertNotNull(parsed["Wait"])

		val nodeDefs = parsed["AbstractBehaviourNodeDefs"]
		assertNotNull(nodeDefs)
		assertTrue(nodeDefs is ReferenceDefinition)
		assertEquals(5, nodeDefs.contentsMap.size)

		val behaviourTreeDef = parsed["BehaviourTree"]
		assertNotNull(behaviourTreeDef)
		assertTrue(behaviourTreeDef is GraphStructDefinition)
		assertEquals(true, behaviourTreeDef.flattenData)
		assertEquals(true, behaviourTreeDef.allowCircularLinks)
		assertEquals(true, behaviourTreeDef.allowReferenceLinks)
		assertEquals("NodeMap", behaviourTreeDef.nodeStoreName)
		assertEquals(5, behaviourTreeDef.nodeDefs.size)
	}

	@Test
	fun createLarge() {
		val defRaw = getDefFile()
		val parsed = defRaw.parseProjectAndResolve()

		val behaviourTreeDef = parsed["BehaviourTree"]
		assertNotNull(behaviourTreeDef)

		val document = DataDocument("")
		val item = behaviourTreeDef.createItem(document)

		assertTrue(item is GraphStructItem)
		assertNotNull(item.nodeStore)
	}

	@Test
	fun loadLarge() {
		val dataXml = """
			<BehaviourTree meta:X="0" meta:Y="0" GUID="d0c70fe6-69bf-4558-a133-f017c5ce17c4" xmlns:meta="Editor">
				<Root meta:RefKey="RunOneRandomly">9c535ca3-cc30-490a-adf2-b299f9395c45</Root>
				<NodeMap>
					<RunOneRandomly meta:X="240" meta:Y="0" GUID="9c535ca3-cc30-490a-adf2-b299f9395c45">
						<!--25% chance to explore-->
						<Wait>
							<classID>Wait</classID>
							<Count>1</Count>
						</Wait>
						<Wait>
							<classID>Wait</classID>
							<Count>1</Count>
						</Wait>
						<Wait>
							<classID>Wait</classID>
							<Count>1</Count>
						</Wait>
						<Node>
							<classID>Node</classID>
							<Node meta:RefKey="RunUntilNotCompleted">875dbd75-563f-4f6b-95aa-5028fded4b69</Node>
						</Node>
					</RunOneRandomly>
					<RunUntilNotCompleted meta:X="555" meta:Y="0" GUID="875dbd75-563f-4f6b-95aa-5028fded4b69">
						<!--Set explore pos if not set-->
						<Branch>
							<classID>Branch</classID>
							<Branches>
								<ConditionAndNode>
									<Condition>explorePos==0</Condition>
									<Node meta:RefKey="RunUntilNotCompleted">004dd7df-8f40-4bac-b359-87a520903c1d</Node>
								</ConditionAndNode>
							</Branches>
						</Branch>
						<!--Move to explore pos, then clear-->
						<MoveTo>
							<classID>MoveTo</classID>
							<Key>explorePos</Key>
						</MoveTo>
						<ClearValue>
							<classID>ClearValue</classID>
							<Key>explorePos</Key>
						</ClearValue>
					</RunUntilNotCompleted>
					<RunUntilNotCompleted meta:X="1050" meta:Y="0" GUID="004dd7df-8f40-4bac-b359-87a520903c1d">
						<!--Select random tile-->
						<GetAllVisible>
							<classID>GetAllVisible</classID>
							<Type>Tiles</Type>
							<Key>tiles</Key>
						</GetAllVisible>
						<PickOneFrom>
							<classID>PickOneFrom</classID>
							<Input>tiles</Input>
							<Output>explorePos</Output>
							<Condition>random</Condition>
						</PickOneFrom>
					</RunUntilNotCompleted>
				</NodeMap>
			</BehaviourTree>
		""".trimIndent().parseXml().toXDocument()

		val defRaw = getDefFile()
		val parsed = defRaw.parseProjectAndResolve()

		val behaviourTreeDef = parsed["BehaviourTree"]
		assertNotNull(behaviourTreeDef)

		val document = DataDocument("")
		val loaded = behaviourTreeDef.loadItem(document, dataXml.root)

		assertTrue(loaded is GraphStructItem)
	}
}