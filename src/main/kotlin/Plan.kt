import docs_to_tests.extractTestsConfiguration
import extractor.extractDocs
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.DynamicTest

class DocsExtract(
    val currentFile: KtFile,
    val documentations: List<Pair<KtElement, KDoc>>
)

class TestConfiguration(
    private val testName: String,
    private val testSnippet: String,
    private val testSettings: TestSettings
) {
    fun toDynamicTest(): DynamicTest =
        DynamicTest.dynamicTest(testName) {
            testSettings.executeTestSnippet(testSnippet)
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


