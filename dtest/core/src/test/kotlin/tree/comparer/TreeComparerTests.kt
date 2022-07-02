package tree.comparer

import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class TreeComparerTests {
    private val kotlinParserProject by lazy {
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

    private fun createKtFile(
        file: File
    ): KtFile {
        val codeString = file.readLines().joinToString("\n")
        val fileName = file.name
        return PsiManager.getInstance(kotlinParserProject).findFile(
            LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
        ) as KtFile
    }

    private fun compareFilesInFolder(folder: File): Boolean {
        val expectedFile = createKtFile(folder.resolve("expected.kt"))
        val actualFile = createKtFile(folder.resolve("actual.kt"))
        return TreeComparer().compare(expectedFile, actualFile)
    }

    @Test
    fun `compare identical trees`() {
        assertTrue(compareFilesInFolder(File("tree-comparing").resolve("identical")))
    }

    @Test
    fun `compare differing only in spaces trees`() {
        assertTrue(compareFilesInFolder(File("tree-comparing").resolve("diff-in-spaces")))
    }

    @Test
    fun `compare different trees`() {
        assertFalse(compareFilesInFolder(File("tree-comparing").resolve("different")))
    }
}