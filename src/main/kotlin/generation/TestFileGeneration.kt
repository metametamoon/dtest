package generation

import TestInfo
import org.jetbrains.kotlin.name.FqName

fun generateTestFile(
    @Suppress("unused")
    testInfos: List<TestInfo>,
    packageFqName: FqName
): List<String> {
    return """
        import util.shouldBe

        private class f {
            @Test
            fun `1`() {
                f() shouldBe 42
            }
        }
    """.trimIndent().split("\n")
}