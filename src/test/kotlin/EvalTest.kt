import org.junit.jupiter.api.TestFactory

class EvalTest {
    @TestFactory
    fun eval() = runTestsFromFile("src/main/kotlin/Sum.kt")
}