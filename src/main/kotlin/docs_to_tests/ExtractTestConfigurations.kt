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

private fun extractName(element: KtElement): String {
    val name = when (element) {
        is KtFunction -> element.fqName?.asString() ?: "unnamed"
        is KtProperty -> element.fqName?.asString() ?: "unnamed"
        else -> element.name ?: "unnamed"
    }
    return name
}
