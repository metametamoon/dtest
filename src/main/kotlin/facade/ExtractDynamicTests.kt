package facade

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import docs_to_tests.TestConfiguration
import docs_to_tests.extractTestsConfiguration
import extractor.ExtractedDocs
import extractor.extractDocs
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.junit.jupiter.api.DynamicTest

fun extractDynamicTests(path: String): Result<List<DynamicTest>, Exception> =
    binding {
        setIdeaIoUseFallback()
        val extractedDocs: ExtractedDocs = extractDocs(path).bind()
        val testConfigurations: List<TestConfiguration> =
            extractTestsConfiguration(extractedDocs)
        testConfigurations.map(TestConfiguration::toDynamicTest)
    }