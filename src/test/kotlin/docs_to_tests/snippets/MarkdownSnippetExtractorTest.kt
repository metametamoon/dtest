package docs_to_tests.snippets

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class MarkdownSnippetExtractorTest {
    private val markdownExtractor = MarkdownSnippetExtractor()
    private val oneLine = """
        /**
         * Must return 42.
         * Tests: ```f() shouldBe 42```
         */
    """.trimIndent().split("\n")

    private val multiLine = """
        /**
         * Must return 42.
         * Tests: ```
         * val x = f()
         * x shouldBe 42
         * ```
         */
    """.trimIndent().split("\n")

    private val multiTest = """
        /**
         * Must return 42.
         * Tests: 
         * ```f() shouldBe 42```
         * ```f() != 43```
         */
    """.trimIndent().split("\n")

    @Test
    fun `one test on one line`() {
        Assertions.assertEquals(
            markdownExtractor.extractCodeSnippets(oneLine),
            listOf(CodeSnippet("f() shouldBe 42"))
        )
    }

    @Test
    fun `one test on multiple lines`() {
        Assertions.assertEquals(
            markdownExtractor.extractCodeSnippets(multiLine),
            listOf(CodeSnippet("\nval x = f()\nx shouldBe 42\n"))
        )
    }

    @Test
    fun `multiple tests`() {
        Assertions.assertEquals(
            markdownExtractor.extractCodeSnippets(multiTest),
            listOf(
                CodeSnippet("f() shouldBe 42"),
                CodeSnippet("f() != 43")
            )
        )
    }
}