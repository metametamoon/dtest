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
        extractTestLines(kDoc).map { snippet ->
            CodeSnippet(snippet, DefaultTestSettings)
        }

    /**
     * Returns the list of code lines; a code line is a part of a single
     * text row after the ">>>". Each line here is considered independent
     * and will be run separately.
     */
    private fun extractTestLines(
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