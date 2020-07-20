package test.sde.utils

import sde.util.XData
import sde.util.XDocument
import sde.util.XElement
import kotlin.test.Test
import sde.utils.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
		assertEquals("Child", child.name)
		assertEquals(1, child.children.size)

		val data = child.children[0]
		assertTrue(data is XElement)
		assertEquals(0, data.children.size)
		assertEquals("Cheese", data.value)

		val asString = xdata.toString()
		assertEquals(rawXml, asString)
	}

	@Test
	fun attributes() {

	}
}