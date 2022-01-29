import com.github.michaelbull.result.Result
import extractor.*
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import java.io.File
import javax.script.ScriptEngine

fun runTestsFromFile(path: String): List<DynamicTest> {
    setIdeaIoUseFallback()
    val project = createNewProject()
    val file = File(path)
    val text = file.readText().replace("\r\n", "\n")
    val ktFile = createKtFile(text, file.absolutePath, project)
    val tests = extractTests(ktFile)
    return tests.map(Test::toDynamicTest)
}

data class Assertion(
    val sourceLocation: SourceLocation,
    val equalityParts: EqualityParts,
) {
    fun execute(engine: ScriptEngine) {
        Assertions.assertEquals(
            engine.eval(equalityParts.expectedExpression),
            engine.eval(equalityParts.actualExpression),
            "$sourceLocation"
        )
    }
}

fun extractAssertions(
    kDoc: KDoc,
    sourceLocation: SourceLocation
): List<Assertion> {
    val documentationText = kDoc.text?.split("\n") ?: return listOf()
    val assertions = documentationText.mapNotNull { row ->
        val regex = """>>>(.*)""".toRegex()
        regex.find(row)?.let { match ->
            match.groups[1]?.value
        }
    }
    return assertions.map(::extractEqualityParts)
        .mapNotNull(Result<EqualityParts, String>::component1)
        .map { equalityParts -> Assertion(sourceLocation, equalityParts) }
}
