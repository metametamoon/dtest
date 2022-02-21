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
import org.junit.jupiter.api.Test
import java.io.File

internal val globalKotlinParserOnlyProject by lazy {
    val configuration = CompilerConfiguration()
    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE
    )

    KotlinCoreEnvironment.createForProduction(
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

    @Test
    fun `contains test code`() {
        val properlyGeneratedCode = createKtFile(
            File("testData/sum/TestSum.kt").readText().replace("\r\n", "\n"),
            "TestSum",
            globalKotlinParserOnlyProject
        )
        val actualGeneratedCode = generateTestFile(
            listOf(
                TestInfo("f", listOf(CodeSnippet("f() shouldBe 42")))
            ), FqName("")
        ).joinToString("\n")

        val actualFile = createKtFile(
            actualGeneratedCode,
            "TestSum",
            globalKotlinParserOnlyProject
        )
        val expectedAst =
            DebugUtil.psiToString(properlyGeneratedCode, false, false)
        val actualAst = DebugUtil.psiToString(actualFile, false, false)
        Assertions.assertEquals(expectedAst, actualAst)
    }
}
