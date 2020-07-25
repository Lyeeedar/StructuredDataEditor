package test.sde.data.definition

import kotlin.test.Test
import kotlin.test.assertEquals

class DependencyOrderTest
{
    @Test
    fun referencingSelf() {
        val xml = """
            <Definitions xmlns:meta="Editor">
                <Data Name="Thing" meta:RefKey="Struct">
                    <Data Name="Child" Keys="Thing" meta:RefKey="Reference" />
                </Data>
            </Definitions>
        """.trimIndent()

        val defMap = xml.parseProjectAndResolve()
        assertEquals(1, defMap.size)
    }

    @Test
    fun defKeyKeysSelf() {
        val xml = """
            <Definitions xmlns:meta="Editor">
                <Data Name="Things" Keys="Thing" meta:RefKey="ReferenceDef" />
                <Data Name="Thing" meta:RefKey="Struct">
                    <Data Name="Child" DefKey="Things" meta:RefKey="Reference" />
                </Data>
            </Definitions>
        """.trimIndent()

        val defMap = xml.parseProjectAndResolve()
        assertEquals(2, defMap.size)
    }
}