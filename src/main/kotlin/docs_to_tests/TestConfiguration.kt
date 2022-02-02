package docs_to_tests

import org.junit.jupiter.api.DynamicTest

class TestConfiguration(
    private val testName: String,
    private val testSnippet: String,
    private val testSettings: TestSettings
) {
    fun toDynamicTest(): DynamicTest = DynamicTest.dynamicTest(testName) {
        testSettings.executeTestSnippet(testSnippet)
    }
}