package docs_to_tests

import org.jetbrains.kotlin.kdoc.psi.api.KDoc

data class CodeSnippet(
    val snippet: String, val settings: TestSettings
)

interface CodeSnippetsExtractor {
    fun extractCodeSnippets(kDoc: KDoc): List<CodeSnippet>
}

object DefaultCodeSnippetsExtractor : CodeSnippetsExtractor {
    override fun extractCodeSnippets(kDoc: KDoc): List<CodeSnippet> =
        extractCodeBlocks(kDoc).map { snippet ->
            CodeSnippet(snippet, DefaultTestSettings)
        }

    private fun extractCodeBlocks(
        kDoc: KDoc
    ): List<String> {
        val documentationText = kDoc.text?.split("\n") ?: return listOf()
        val assertions = documentationText.mapNotNull { row ->
            val regex = """>>>(.*)""".toRegex()
            regex.find(row)?.let { match ->
                match.groups[1]?.value
            }
        }
        return assertions
    }
}