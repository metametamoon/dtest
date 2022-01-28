package extractor

import extractAssertions
import org.jetbrains.kotlin.psi.KtFile
import javax.script.ScriptEngineManager


val engine by lazy { ScriptEngineManager().getEngineByExtension("kts")!! }

fun extractTests(ktFile: KtFile): List<Test> {
    val docs = extractFunctionsAndDocs(ktFile)
    val assertions =
        docs.map { (doc, function) -> extractAssertions(doc) to function }
    return assertions.map { (assertions, _) ->
        Test { assertions.map { assertion -> assertion.execute(engine) } }
    }
}