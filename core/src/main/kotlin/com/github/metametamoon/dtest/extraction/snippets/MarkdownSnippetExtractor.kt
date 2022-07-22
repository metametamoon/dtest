package com.github.metametamoon.dtest.extraction.snippets

data class CodeSnippet(
    val snippet: String
)

const val testCode = "testCode"


private fun markdownCodePattern(captureGroupName: String) = "```\\s*(?<$captureGroupName>.*?)\\s*```"
class MarkdownSnippetExtractor {
    private val kdocTestPattern =
        markdownCodePattern(testCode).toRegex(RegexOption.DOT_MATCHES_ALL)

    fun extractCodeSnippets(docText: List<String>): List<CodeSnippet> {
        val trimmedDoc = docText.joinToString("\n")
        val partWithTests = trimmedDoc.substringAfter("<!--dtests-->", "")
        return kdocTestPattern.findAll(partWithTests).map { match ->
            val matchGroup =
                match.groups[testCode] ?: throw InternalError("No group named $testCode ${kdocTestPattern.pattern}")
            val code = matchGroup.value
            CodeSnippet(code)
        }.toList()
    }

    fun getLinesWithTestStarts(docText: List<String>): List<Int> = getLineRanges(docText).map { it.first }

    fun getLineRanges(docText: List<String>): List<IntRange> {
        val trimmedDoc = docText.joinToString("\n")
        val dtestDelimiter = "<!--dtests-->"
        val partWithTests = trimmedDoc.substringAfter(dtestDelimiter, "")
        val newLinesBeforeTrimmed = trimmedDoc.substringBefore(dtestDelimiter, "").count { it == '\n' }
        return kdocTestPattern.findAll(partWithTests).map { match ->
            val newLinesOfStartAfterTrimmed = partWithTests.substring(0 until match.range.first).count { it == '\n' }
            val newLinesOfEndAfterTrimmed = partWithTests.substring(0 until match.range.last).count { it == '\n' }
            (newLinesOfStartAfterTrimmed + newLinesBeforeTrimmed)..(newLinesOfEndAfterTrimmed + newLinesBeforeTrimmed)
        }.toList()
    }

}

