import com.github.michaelbull.result.Result
import org.jetbrains.kotlin.com.intellij.openapi.editor.Document
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.junit.Assert
import java.io.File
import javax.script.ScriptEngineManager

fun runTestsFromFile(path: String) {
    val project = createNewProject()
    val file = File(path)
    val text = file.readText().replace("\r\n", "\n")
    val ktFile = createKtFile(text, file.absolutePath, project)
    val document = ktFile.containingFile.viewProvider.document!!

    val docs = extractFunctionsAndDocs(ktFile).map { it.first }
    val assertions = docs.flatMap(::extractAssertions)
    val engine = ScriptEngineManager().getEngineByExtension("kts")!!
    assertions.forEachIndexed { index, (equalityParts, offset) ->
        val (actual, expected) = equalityParts
        val lineNumber = document.getLineNumber(offset) + 1
        Assert.assertEquals(
            "Error in test suite (${file.absolutePath}:$lineNumber)\n",
            engine.eval(expected),
            engine.eval(actual)
        )
        val testNumber = index + 1
        println("$testNumber - successful")
    }
}

data class Assertion(
    val equalityParts: EqualityParts,
    val testSuiteOffset: Int
)

fun extractAssertions(kDoc: KDoc): List<Assertion> {
    val documentationText = kDoc.text?.split("\n") ?: return listOf()
    val assertions = documentationText.mapNotNull { row ->
        val regex = """>>>(.*)""".toRegex()
        regex.find(row)?.let { match ->
            match.groups[1]?.value
        }
    }
    return assertions.map(::extractEqualityParts)
        .mapNotNull(Result<EqualityParts, String>::component1)
        .map { equalityParts -> Assertion(equalityParts, kDoc.startOffset) }
}
