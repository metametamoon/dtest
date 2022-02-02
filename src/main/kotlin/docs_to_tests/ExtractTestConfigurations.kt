package docs_to_tests

import extractor.ExtractedDocs
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty

fun extractTestsConfiguration(
    extractedDocs: ExtractedDocs,
    codeSnippetsExtractor: CodeSnippetsExtractor = DefaultCodeSnippetsExtractor
): List<TestConfiguration> {
    return extractedDocs.documentations.flatMap { (element, kDoc) ->
        val name = extractName(element)
        val testSnippets = codeSnippetsExtractor.extractCodeSnippets(kDoc)
        testSnippets.map { (snippet, settings) ->
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
