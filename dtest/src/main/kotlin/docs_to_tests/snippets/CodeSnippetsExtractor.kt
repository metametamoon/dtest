package docs_to_tests.snippets

data class CodeSnippet(
    val snippet: String
)

interface CodeSnippetsExtractor {
    fun extractCodeSnippets(
        docText: List<String>,
    ): List<CodeSnippet>
}

