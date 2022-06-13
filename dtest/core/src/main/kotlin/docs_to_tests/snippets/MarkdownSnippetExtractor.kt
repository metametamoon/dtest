package docs_to_tests.snippets

import util.trimDocs

data class CodeSnippet(
    val snippet: String,
    val importsSnippet: String?
)

const val testCode = "testCode"
const val importsCode = "imports"


private fun markdownCodePattern(name: String) = "```(?<$name>.*?)```"
private fun optional(pattern: String) = "(?:$pattern)?"
private fun inMarkdownComment(pattern: String) = "<!--$pattern-->"
class MarkdownSnippetExtractor {
    private val kdocTestPattern =
        (optional(inMarkdownComment(markdownCodePattern(importsCode))) + "\\s*" + markdownCodePattern(testCode))
            .toRegex()

    fun extractCodeSnippets(docText: List<String>): List<CodeSnippet> {
        val trimmedDoc = docText.trimDocs().joinToString("\n")
        val partWithTests = trimmedDoc.substringAfter("Tests:")
        return kdocTestPattern.findAll(partWithTests).map { match ->
            val code = match.groups[testCode]?.value
                ?: throw InternalError("No group named $testCode ${kdocTestPattern.pattern}")
            val imports = match.groups[importsCode]?.value
            CodeSnippet(code, imports)
        }.toList()
    }
}

