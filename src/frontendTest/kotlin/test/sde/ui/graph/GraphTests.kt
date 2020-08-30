package test.sde.ui.graph

import sde.data.DataDocument
import sde.data.item.GraphStructItem
import sde.ui.graph.Graph
import sde.utils.parseXml
import sde.utils.toXDocument
import test.sde.data.definition.parseProjectAndResolve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GraphTests
{
	fun getDocument(): DataDocument {
		val xmlDef = """
			<Definitions xmlns:meta="Editor">
				<Definition Name="Block" FlattenData="True" meta:RefKey="GraphStruct">
					<Data Name="Root" Keys="Node" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="Node" meta:RefKey="GraphStructDef">
					<Data Name="Link" Keys="Node" meta:RefKey="GraphReference" />
				</Definition>
			</Definitions>
		""".trimIndent()

		val xmlData = """
			<Block xmlns:meta="Editor">
				<Root>guid1</Root>
				<Nodes>
					<Node meta:X="0" meta:Y="20" GUID="guid1" meta:RefKey="Node">
						<Link>guid2</Link>
					</Node>
					<Node meta:X="50" meta:Y="70" GUID="guid2" meta:RefKey="Node">
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
		dataDoc.root = data

		assertNotNull(data.nodeStore)
		assertEquals(2, data.nodeStore!!.children.size)

		return dataDoc
	}

	@Test
	fun graphNodeSearch() {
		val doc = getDocument()

		val graph = Graph(doc)

		val graphItems = graph.getGraphNodeItems().toList()
		assertEquals(3, graphItems.size)

		val graphNodes = graph.getGraphNodes().toList()
		assertEquals(3, graphNodes.size)

		val rootNode = graphNodes[0]
		val guid1Node = graphNodes[1]
		val guid2Node = graphNodes[2]

		assertEquals("guid1", guid1Node.graphItem.guid)
		assertEquals("guid2", guid2Node.graphItem.guid)

		assertEquals(0.0, rootNode.graphItem.nodePositionX)
		assertEquals(0.0, rootNode.graphItem.nodePositionY)

		assertEquals(0.0, guid1Node.graphItem.nodePositionX)
		assertEquals(20.0, guid1Node.graphItem.nodePositionY)

		assertEquals(50.0, guid2Node.graphItem.nodePositionX)
		assertEquals(70.0, guid2Node.graphItem.nodePositionY)
	}
}