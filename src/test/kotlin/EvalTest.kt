import com.github.michaelbull.result.unwrap
import facade.extractDynamicTests
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test

private fun List<DynamicTest>.matchRegex(regex: Regex): List<DynamicTest> =
    this.filter { it.displayName.matches(regex) }

private fun List<DynamicTest>.executeAll() = forEach { it.executable.execute() }

class EvalTest {
    private val dynamicTests =
        extractDynamicTests("src/test/kotlin/Sum.kt").unwrap()

    @Test
    fun `must not fail`() {
        dynamicTests.matchRegex("sum.*".toRegex()).executeAll()
    }

    @Test
    fun `must fail`() {
        dynamicTests.matchRegex("Const.*".toRegex()).forEach { test ->
            try {
                test.executable.execute()
                Assertions.fail("${test.displayName} should've thrown.")
            } catch (_: Throwable) {
            }
        }
    }
}