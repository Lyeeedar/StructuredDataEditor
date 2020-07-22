package test.sde.utils

import sde.utils.parseCategorisedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CategorisedStringTest
{
	@Test
	fun parseNonCategorised() {
		val source = "this,is,a,csv"
		val parsed = source.parseCategorisedString()

		assertEquals(1, parsed.size)

		val block = parsed[""]
		assertNotNull(block)
		assertEquals(4, block.size)
		assertEquals("this", block[0])
		assertEquals("csv", block[3])
	}

	@Test
	fun parseCategorised() {
		val source = "title(this,is,data),title2(more,data)"
		val parsed = source.parseCategorisedString()

		assertEquals(2, parsed.size)

		assertNull(parsed[""])

		val titleBlock = parsed["title"]
		assertNotNull(titleBlock)
		val title2Block = parsed["title2"]
		assertNotNull(title2Block)

		assertEquals(3, titleBlock.size)
		assertEquals("this", titleBlock[0])
		assertEquals("data", titleBlock[2])

		assertEquals(2, title2Block.size)
		assertEquals("more", title2Block[0])
		assertEquals("data", title2Block[1])
	}
}