import com.github.michaelbull.result.Result
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.junit.Assert
import java.io.File
import javax.script.ScriptEngineManager

fun runTestsFromFile(path: String) {
    val project = createNewProject()
    val text = File(path).readText().replace("\r\n", "\n")
    val ktFile = createKtFile(text, path, project)
    val docs = extractFunctionsAndDocs(ktFile).map { it.first }
    val assertions: List<Pair<String, String>> =
        docs.flatMap(::extractAssertions)
    val engine = ScriptEngineManager().getEngineByExtension("kts")!!
    assertions.forEachIndexed { index, (actual, expected) ->
        Assert.assertEquals(engine.eval(expected), engine.eval(actual))
        val testNumber = index + 1
        println("$testNumber - successful")
    }
}

fun extractAssertions(kDoc: KDoc): List<Pair<String, String>> {
    val documentationText = kDoc.text?.split("\n") ?: return listOf()
    val assertions = documentationText.mapNotNull { row ->
        val regex = """>>>(.*)""".toRegex()
        regex.find(row)?.let { match ->
            match.groups[1]?.value
        }
    }
    return assertions.map(::extractEqualityParts)
        .mapNotNull(Result<Pair<String, String>, String>::component1)
}
