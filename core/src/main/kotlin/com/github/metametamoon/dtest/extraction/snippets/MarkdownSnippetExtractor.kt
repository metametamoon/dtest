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
}

