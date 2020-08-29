package test.sde.data.definition

import sde.data.DataDocument
import sde.data.item.GraphReferenceItem
import sde.data.item.GraphStructItem
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.test.*

class GraphReferenceDefinitionTest
{
	@Test
	fun testLoad() {
		val xmlDef = """
			<Definitions xmlns:meta="Editor">
				<Definition Name="Block" FlattenData="True" meta:RefKey="GraphStruct">
					<Data Name="Root" Keys="Node" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="Node" meta:RefKey="GraphStructDef">
				</Definition>
			</Definitions>
		""".trimIndent()

		val xmlData = """
			<Block xmlns:meta="Editor">
				<Root>guid1</Root>
				<Nodes>
					<Node GUID="guid1" meta:RefKey="Node">
					</Node>
				</Nodes>
			</Block>
		""".trimIndent().parseXml().toXDocument()

		val parsed = xmlDef.parseProjectAndResolve()

		val def = parsed["Block"]
		assertNotNull(def)

		val dataDoc = DataDocument("")
		val data = dataDoc.loadItem(def, xmlData.root)

		assertTrue(data is GraphStructItem)

		val nodeStore = data.nodeStore
		assertNotNull(nodeStore)
		assertEquals(1, nodeStore.children.size)

		assertTrue(data.def.flattenData)
		assertEquals("Nodes", data.def.nodeStoreName)

		val root = data.children[0]
		assertEquals("Root", root.def.name)
		assertTrue(root is GraphReferenceItem)

		assertNull(root.selectedItemGuid)
		assertNotNull(root.createdItem)
		assertEquals(nodeStore.children[0], root.createdItem)
	}
}