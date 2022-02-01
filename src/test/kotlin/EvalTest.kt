import org.junit.jupiter.api.TestFactory

class EvalTest {
    @TestFactory
    fun eval() = extractDynamicTests("src/main/kotlin/Sum.kt")
}