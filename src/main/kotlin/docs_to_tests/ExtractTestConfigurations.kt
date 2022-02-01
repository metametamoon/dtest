package docs_to_tests

import DocsExtract
import TestConfiguration
import TestSettings
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

object DefaultTestSettings : TestSettings {
    private val defaultKtsEngine =
        ScriptEngineManager().getEngineByExtension("kts")!!

    override fun executeTestSnippet(testSnippet: String) {
        defaultKtsEngine.getBindings(ScriptContext.ENGINE_SCOPE).clear()
        defaultKtsEngine.eval("import org.junit.jupiter.api.Assertions.assertEquals")
        defaultKtsEngine.eval(testSnippet)
    }
}

fun extractTestsConfiguration(
    extractedDocs: DocsExtract,
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
