import extractor.extractDocs
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.DynamicTest

class DocsExtract(
    val currentFile: KtFile,
    val documentations: List<Pair<KtElement, KDoc>>
)

class TestConfiguration(
    private val testName: FqName,
    private val testSnippet: List<String>,
    private val testSettings: TestSettings
) {
    fun toDynamicTest(): DynamicTest =
        DynamicTest.dynamicTest(testName.asString()) {
            testSettings.executeTestSnippet(testSnippet.joinToString("\n"))
        }
}

interface TestSettings {
    fun executeTestSnippet(testSnippet: String)
}

@Suppress("unused")
fun extractDynamicTests(path: String): List<DynamicTest> {
    val extractedDocs: DocsExtract = extractDocs(path)
    val testConfigurations: List<TestConfiguration> =
        extractTestsConfiguration(extractedDocs)
    return testConfigurations.map(TestConfiguration::toDynamicTest)
}

fun extractTestsConfiguration(extractedDocs: DocsExtract): List<TestConfiguration> {
    TODO("Not yet implemented")
}

