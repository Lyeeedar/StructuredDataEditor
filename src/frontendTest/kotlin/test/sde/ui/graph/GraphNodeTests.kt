package test.sde.ui.graph

import sde.data.DataDocument
import sde.data.item.CollectionItem
import sde.data.item.GraphReferenceItem
import sde.data.item.GraphStructItem
import sde.ui.graph.Graph
import sde.utils.parseXml
import sde.utils.toXDocument
import test.sde.data.definition.parseProjectAndResolve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GraphNodeTests
{
    fun getDocument(): DataDocument {
        val xmlDef = """
			<Definitions xmlns:meta="Editor">
				<Definition Name="Block" FlattenData="True" AllowCircularLinks="True" meta:RefKey="GraphStruct">
					<Data Name="Root" Keys="Node" Nullable="False" SkipIfDefault="False" meta:RefKey="GraphReference" />
				</Definition>
				<Definition Name="Node" meta:RefKey="GraphStructDef">
					<Data Name="Link" Keys="Node" meta:RefKey="GraphReference" />
                    <Data Name="Name" meta:RefKey="String" />
                    <Data Name="Count" meta:RefKey="Number" />
                    <Data Name="Links" meta:RefKey="Collection">
                        <Data Name="Link" Keys="Node" meta:RefKey="GraphReference" />
                    </Data>
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
                        <Links>
                            <Link>guid1</Link>
                            <Link>guid1</Link>
                        </Links>
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
    fun testPreviewItems() {
        val document = getDocument()

        val graph = Graph(document)

        val graphItems = graph.getGraphNodeItems().toList()
        assertEquals(3, graphItems.size)

        val graphNodes = graph.getGraphNodes().toList()
        assertEquals(3, graphNodes.size)

        val guid1Node = graphNodes[1]
        val guid2Node = graphNodes[2]

        assertEquals("guid1", guid1Node.graphItem.guid)
        assertEquals("guid2", guid2Node.graphItem.guid)

        val guid1Items = guid1Node.getDataItems().toList()
        assertEquals(4, guid1Items.size)

        val links = guid2Node.node.children[3] as CollectionItem
        assertNotNull(links)
        assertEquals(2, links.children.size)
        assertTrue(links.children[0] is GraphReferenceItem)
        assertEquals(2, guid2Node.getDataItems(links).count())

        val guid2Items = guid2Node.getDataItems().toList()
        assertEquals(6, guid2Items.size)
    }
}