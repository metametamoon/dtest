package generation

import TestInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.LightVirtualFile
import docs_to_tests.snippets.CodeSnippet
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

internal val globalKotlinParserOnlyProject by lazy {
    val configuration = CompilerConfiguration()
    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE
    )

    KotlinCoreEnvironment.createForTests(
        Disposer.newDisposable(),
        configuration,
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
}

private class TestGenerationTest {
    private fun createKtFile(
        codeString: String,
        fileName: String,
        project: Project
    ): KtFile {
        return PsiManager.getInstance(project).findFile(
            LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
        ) as KtFile
    }


    fun checkFileGeneration(
        expectedCodeGenerationFileName: String,
        testInfos: List<TestInfo>,
        fqName: FqName,
    ) {
        val file = File(expectedCodeGenerationFileName)
        println(file.absolutePath)
        val properlyGeneratedCode = createKtFile(
            file.readText()
                .replace("\r\n", "\n"),
            expectedCodeGenerationFileName.substringAfterLast("/"),
            globalKotlinParserOnlyProject
        )
        val actualGeneratedCode =
            generateTestFile(testInfos, fqName, "org.junit.jupiter.api.Test").joinToString("\n")

        val actualFile = createKtFile(
            actualGeneratedCode,
            expectedCodeGenerationFileName.substringAfterLast("/"),
            globalKotlinParserOnlyProject
        )
        val expectedAst =
            DebugUtil.psiToString(properlyGeneratedCode, false, false)
        val actualAst = DebugUtil.psiToString(actualFile, false, false)
        Assertions.assertEquals(expectedAst, actualAst) {
            println("The code is:\n $actualGeneratedCode")
            "Different asts"
        }
    }

    @Disabled("These tests are ridiculously fragile")
    @Test
    fun `one function with one test`() {
        checkFileGeneration(
            "testData/sum/TestSum.kt",
            listOf(
                TestInfo("f", listOf(CodeSnippet("Assertions.assertEquals(42, f())", null)))
            ), FqName("")
        )
    }

    @Disabled("These tests are ridiculously fragile")
    @Test
    fun `two functions, one test each`() {
        checkFileGeneration(
            "testData/sum/FAndG.kt",
            listOf(
                TestInfo("f", listOf(CodeSnippet("Assertions.assertEquals(42, f())", null))),
                TestInfo("g", listOf(CodeSnippet("Assertions.assertEquals(-42, g())", null)))
            ), FqName("")
        )
    }
}
