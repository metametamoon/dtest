package docs_to_tests

import docs_to_tests.snippets.CodeSnippetsExtractor
import docs_to_tests.snippets.HaskelLikeLinesExtractor
import docs_to_tests.snippets.asText
import extractor.ExtractedDocs
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty

fun extractTestsConfiguration(
    extractedDocs: ExtractedDocs,
    codeSnippetsExtractor: CodeSnippetsExtractor = HaskelLikeLinesExtractor
): List<TestConfiguration> {
    return extractedDocs.documentations.flatMap { (element, kDoc) ->
        val name = extractName(element)
        val docText = kDoc.asText()
        val testSnippets = codeSnippetsExtractor.extractCodeSnippets(docText)
        val settings = DefaultTestSettings
        testSnippets.map { (snippet) ->
            TestConfiguration(name, snippet, settings)
        }
    }
}

private fun extractName(element: KtElement): String =
    when (element) {
        is KtFunction -> element.fqName?.asString()
        is KtProperty -> element.fqName?.asString()
        else -> element.name
    } ?: "unnamed"
