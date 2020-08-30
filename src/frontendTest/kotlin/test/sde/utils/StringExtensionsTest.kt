package test.sde.utils

import sde.utils.*
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

	@Test
	fun testGetDirectory() {
		assertEquals("C:/Users/Philip/Documents", "C:/Users/Philip/Documents/file.txt".getDirectory())
		assertEquals("C:/Users/Philip", "C:/Users/Philip/Documents".getDirectory())
		assertEquals("C:/Users/Philip/Other", "C:/Users/Philip\\Other\\file.txt".getDirectory())
		assertEquals("C:/Users/Philip/Other", "C://Users//Philip\\Other//file.txt".getDirectory())
	}

	@Test
	fun testGetFilename() {
		assertEquals("file.txt", "C:/Users/Philip/Document/file.txt".getFileName())
		assertEquals("file", "C:/Users/Philip/Document/file".getFileName())
		assertEquals("file.txt", "C:\\Users\\Philip\\Document\\file.txt".getFileName())
	}

	@Test
	fun testGetExtension() {
		assertEquals("txt", "C:/Users/Philip/Document/file.txt".getExtension())
		assertEquals("txt", "C:/Users/Philip/Document\\file.txt".getExtension())
		assertEquals("", "C:/Users/Philip/Document/file".getExtension())
	}

	@Test
	fun testPathCombine() {
		assertEquals("C:/Users/Philip/Documents/file.txt", pathCombine("C:/Users", "Philip\\Documents", "file.txt"))
	}

	@Test
	fun testRelPath() {
		assertEquals("Definitions", relPath("C:/Users/Code/Proj/Definitions", "C:/Users/Code/Proj"))
		assertEquals("file.txt", relPath("C:/Users/Philip/Documents/file.txt", "C:/Users/Philip/Documents"))
		assertEquals("file.txt", relPath("C:/Users/Philip/Documents/file.txt", "C:\\Users\\Philip\\Documents"))
		assertEquals("../file.txt", relPath("C:/Users/Philip/file.txt", "C:/Users/Philip/Documents"))
		assertEquals("../Other/file.txt", relPath("C:/Users/Philip/Documents/Other/file.txt", "C:/Users/Philip/Documents/Folder"))
	}

    @Test
    fun removeTags() {
        assertEquals("stuff", "<b>stuff</b>".removeTags())
        assertEquals("stuff", "<span style=\"color: red\">stuff</span>".removeTags())
        assertEquals("stuff, stuff, stuff  ", "<b>stuff</b>,<b> stuff</b>, <b>stuff </b> ".removeTags())
    }
}