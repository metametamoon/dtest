package com.github.metametamoon.dtest.extraction.snippets

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class MarkdownSnippetExtractorTest {
    private val markdownExtractor = MarkdownSnippetExtractor()
    private val oneLine = """
        
        Must return 42.
        <!--dtests-->: ```f() shouldBe 42```
        
    """.trimIndent().split("\n")

    private val oneLineWithStarWithoutSpaceAfter = """
        
        Must return 42.
       
        <!--dtests-->: ```f() shouldBe 42```
        
    """.trimIndent().split("\n")

    private val multiLine = """
        
        Must return 42.
        <!--dtests-->: ```
        val x = f()
        x shouldBe 42
        ```
        
    """.trimIndent().split("\n")

    private val multiTest = """
        
        Must return 42.
        <!--dtests-->: 
        ```f() shouldBe 42```
        ```f() != 43```
        
    """.trimIndent().split("\n")

    @Test
    fun `one test on one line`() {
        Assertions.assertEquals(
            markdownExtractor.extractCodeSnippets(oneLine),
            listOf(CodeSnippet("f() shouldBe 42"))
        )
    }

    @Test
    fun `test asterisk without space after it`() {
        Assertions.assertEquals(
            listOf(CodeSnippet("f() shouldBe 42")),
            markdownExtractor.extractCodeSnippets(oneLineWithStarWithoutSpaceAfter),
        )
    }

    @Test
    fun `one test on multiple lines`() {
        Assertions.assertEquals(
            listOf(CodeSnippet("val x = f()\nx shouldBe 42")),
            markdownExtractor.extractCodeSnippets(multiLine)
        )
    }

    @Test
    fun `multiple tests`() {
        Assertions.assertEquals(
            listOf(
                CodeSnippet("f() shouldBe 42"),
                CodeSnippet("f() != 43")
            ),
            markdownExtractor.extractCodeSnippets(multiTest)
        )
    }
}