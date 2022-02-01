package facade

import docs_to_tests.TestConfiguration
import docs_to_tests.extractTestsConfiguration
import extractor.ExtractedDocs
import extractor.extractDocs
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.junit.jupiter.api.DynamicTest

fun extractDynamicTests(path: String): List<DynamicTest> {
    setIdeaIoUseFallback()
    val extractedDocs: ExtractedDocs = extractDocs(path)
    val testConfigurations: List<TestConfiguration> =
        extractTestsConfiguration(extractedDocs)
    return testConfigurations.map(TestConfiguration::toDynamicTest)
}