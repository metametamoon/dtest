package docs_to_tests.snippets

import util.trimDocs

data class CodeSnippet(
    val snippet: String
)

open class MarkdownSnippetExtractor {
    private val markdownCodePattern =
        "```(.*?)```".toRegex(option = RegexOption.DOT_MATCHES_ALL)

    fun extractCodeSnippets(docText: List<String>): List<CodeSnippet> {
        val trimmedDoc = docText.trimDocs().joinToString("\n")
        val partWithTests = trimmedDoc.substringAfter("Tests:")
        return markdownCodePattern.findAll(partWithTests).map { match ->
            val code = match.groups[1]?.value
                ?: throw InternalError("Group 1 does not exist on pattern ${markdownCodePattern.pattern}")
            CodeSnippet(code)
        }.toList()
    }
}

