package test.sde.utils

import sde.util.XComment
import sde.util.XData
import sde.util.XDocument
import sde.util.XElement
import sde.utils.*
import kotlin.test.*

class XDataTest  {

	@Test
	fun basicParsing() {
		val rawXml = """
			<Root>
				<Child>
					<Data>Cheese</Data>
				</Child>
			</Root>
		""".trimIndent()

		val xdata: XDocument = rawXml.parseXml().toXDocument()

		assertEquals("Root", xdata.root.name)
		assertEquals(1, xdata.root.children.size)

		val child = xdata.root.children[0]
		assertTrue(child is XElement)
		assertEquals(child, xdata.root.getElement("Child"))
		assertEquals("Child", child.name)
		assertEquals(1, child.children.size)

		val data = child.children[0]
		assertTrue(data is XElement)
		assertEquals(data, child.getElement("Data"))
		assertEquals(0, data.children.size)
		assertEquals("Cheese", data.value)

		val asString = xdata.toString()
		assertEquals(rawXml, asString)
	}

	@Test
	fun attributes() {
		val rawXml = """
			<Root xmlns:meta="Editor">
				<Data name="Pie" value="7" meta:RefKey="Number" />
			</Root>
		""".trimIndent()

		val xdata: XDocument = rawXml.parseXml().toXDocument()

		assertEquals(1, xdata.root.children.size)

		val root = xdata.root
		assertEquals(1, root.attributes.size)
		assertEquals("Editor", root.getAttributeValue("xmlns:meta", ""))

		val data = root.getElement("Data")
		assertTrue(data is XElement)
		assertEquals(0, data.children.size)
		assertEquals(3, data.attributes.size)
		assertEquals("Pie", data.getAttributeValue("name", ""))
		assertEquals(7, data.getAttributeValue("value", 0))
		assertEquals("Number", data.getAttributeValue("meta:RefKey", ""))

		val asString = xdata.toString()
		assertEquals(rawXml, asString)
	}

	@Test
	fun comments() {
		val rawXml = """
			<Root>
				<!-- <Fake><Unreal><Nonexistant /></Unreal></Fake> -->
				<Real />
				<!-- Totally
				not
				here -->
				<AlsoReal />
			</Root>
		""".trimIndent()

		val xdata: XDocument = rawXml.parseXml().toXDocument()

		val root = xdata.root
		assertEquals(4, root.children.size)

		assertTrue(root.children[0] is XComment)
		assertTrue(root.children[1] is XElement)
		assertTrue(root.children[2] is XComment)
		assertTrue(root.children[3] is XElement)

		assertNull(root.getElement("Fake"))
		assertNotNull(root.getElement("AlsoReal"))

		val asString = xdata.toString()
		assertEquals(rawXml, asString)
	}
}