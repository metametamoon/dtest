package docs_to_tests.snippets

import org.jetbrains.kotlin.kdoc.psi.api.KDoc

fun KDoc.asText() = text?.split("\n") ?: listOf()


object HaskelLikeLinesExtractor : CodeSnippetsExtractor {
    override fun extractCodeSnippets(docText: List<String>): List<CodeSnippet> =
        extractTestLines(docText).map(::CodeSnippet)

    /**
     * Returns the list of code lines; a code line is a part of a single
     * text row after the ">>>". Each line here is considered independent
     * and will be run separately.
     */
    private fun extractTestLines(documentationText: List<String>): List<String> {
        val assertions = documentationText.mapNotNull { row ->
            val regex = """>>>(.*)""".toRegex()
            regex.find(row)?.let { match ->
                match.groups[1]?.value
            }
        }
        return assertions
    }
}