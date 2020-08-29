package test.sde.data.definition

import sde.data.DataDocument
import sde.data.Project
import sde.data.definition.*
import sde.data.item.*
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TimelineDefinitionTest {
	@Test
	fun testParse()
	{
		val xml = """
			<Data Name="Colour" MinCount="1" xmlns:meta="Editor" meta:RefKey="Timeline">
                <Keyframe Name="Keyframe" meta:RefKey="Keyframe">
                    <Time Name="Time" Min="0" Max="1" SkipIfDefault="False" meta:RefKey="Number" />
                    <Data Name="Colour" SkipIfDefault="False" meta:RefKey="Colour" />
                </Keyframe>
            </Data>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")
		def.resolve__test()

		assertTrue(def is TimelineDefinition)
		assertEquals(1, def.contents.size)
		assertEquals(1, def.contents[0].second.size)
		assertTrue(def.contents[0].second[0] is KeyframeDefinition)
	}

	@Test
	fun testDefKey()
	{
		val xml = """
			<Definitions xmlns:meta="Editor">
                <Data Name="ColourAction" meta:RefKey="Keyframe">
                    <Time Name="Time" Min="0" Max="1" SkipIfDefault="False" meta:RefKey="Number" />
                    <Data Name="Colour" SkipIfDefault="False" meta:RefKey="Colour" />
                </Data>
				<Data Name="NumberAction" meta:RefKey="Keyframe">
                    <Time Name="Time" Min="0" Max="1" SkipIfDefault="False" meta:RefKey="Number" />
                    <Data Name="Number" SkipIfDefault="False" meta:RefKey="Number" />
                </Data>
				<Data Name="Actions" Keys="ColourAction,NumberAction" meta:RefKey="ReferenceDef" />
				<Data Name="ActionTimeline" DefKey="Actions" meta:RefKey="Timeline" />
			</Definitions>
		""".trimIndent()

		val defMap = xml.parseProjectAndResolve()

		assertEquals(4, defMap.size)

		val timelineDef = defMap["ActionTimeline"]
		assertNotNull(timelineDef)

		assertTrue(timelineDef is TimelineDefinition)
		assertEquals("ActionTimeline", timelineDef.name)
		assertEquals(1, timelineDef.contents.size)
		assertEquals(2, timelineDef.contents[0].second.size)
		assertEquals("ColourAction", timelineDef.contents[0].second[0].name)
		assertEquals("NumberAction", timelineDef.contents[0].second[1].name)
	}

	@Test
	fun testLoad() {
		val xml = """
			<Data Name="Number" MinCount="1" xmlns:meta="Editor" meta:RefKey="Timeline">
                <Keyframe Name="Keyframe" meta:RefKey="Keyframe">
                    <Time Name="Time" Min="0" Max="1" SkipIfDefault="False" meta:RefKey="Number" />
                    <Data Name="Alpha" Min="0" Max="1" Default="1" SkipIfDefault="False" meta:RefKey="Number" />
                </Keyframe>
            </Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Number>
				<Keyframe>
					<Time>0</Time>
					<Alpha>0</Alpha>
				</Keyframe>
				<Keyframe>
					<Time>1</Time>
					<Alpha>1</Alpha>
				</Keyframe>
			</Number>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")
		def.resolve__test()

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertTrue(data is TimelineItem)
		assertEquals("Number (2)", data.name)
		assertEquals(2, data.children.size)

		val keyframe1 = data.children[0]
		assertTrue(keyframe1 is KeyframeItem)
		assertEquals(2, keyframe1.children.size)
	}

	@Test
	fun testSave() {
		val xml = """
			<Data Name="Number" MinCount="1" xmlns:meta="Editor" meta:RefKey="Timeline">
                <Keyframe Name="Keyframe" meta:RefKey="Keyframe">
                    <Time Name="Time" Min="0" Max="1" SkipIfDefault="False" meta:RefKey="Number" />
                    <Data Name="Alpha" Min="0" Max="1" Default="1" SkipIfDefault="False" meta:RefKey="Number" />
                </Keyframe>
            </Data>
		""".trimIndent().parseXml().toXDocument()

		val dataXml = """
			<Number>
				<Keyframe>
					<Time>0</Time>
					<Alpha>0</Alpha>
				</Keyframe>
				<Keyframe>
					<Time>1</Time>
					<Alpha>1</Alpha>
				</Keyframe>
			</Number>
		""".trimIndent().parseXml().toXDocument()

		val def = AbstractDataDefinition.load(xml, "")
		def.resolve__test()

		val dataDoc = DataDocument("")
		val data = def.loadItem(dataDoc, dataXml.root)

		assertEquals("""
			<Number>
				<Keyframe>
					<Time>0</Time>
					<Alpha>0</Alpha>
				</Keyframe>
				<Keyframe>
					<Time>1</Time>
					<Alpha>1</Alpha>
				</Keyframe>
			</Number>
		""".trimIndent(), data.def.saveItem(data).toString())
	}
}