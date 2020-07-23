package test.sde.utils

import sde.utils.hex2Rgb
import sde.utils.parseCategorisedString
import sde.utils.rgb2Hex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StringExtensionsTest
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

    @Test
    fun testRgb2Hex() {
        assertEquals("#ffffff", "255,255,255".rgb2Hex())
        assertEquals("#4287f5", "66,135,245".rgb2Hex())
        assertEquals("#000000", "0,0,0".rgb2Hex())
        assertEquals("#9c0c0c", "156,12,12".rgb2Hex())
    }

    @Test
    fun testHex2Rgb() {
        assertEquals("255,255,255", "#ffffff".hex2Rgb())
        assertEquals("66,135,245", "#4287f5".hex2Rgb())
        assertEquals("0,0,0", "#000000".hex2Rgb())
        assertEquals("156,12,12", "#9c0c0c".hex2Rgb())
    }
}