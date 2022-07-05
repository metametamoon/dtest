package functional

import DtestFileGenerator
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
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import tree.comparer.TreeComparer
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.relativeTo

class FunctionalTests {
    private val kotlinParserProject = run {
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

    @TestFactory
    fun createTestsFromFolder(): List<DynamicTest> {
        return File("functional-tests").listFiles().filterNotNull().map { file ->
            generateFunctionalTest(file)
        }
    }

    private val treeComparer = TreeComparer()

    private fun generateFunctionalTest(testFolder: File): DynamicTest {
        val facade = DtestFileGenerator("kotlin.test.Test")
        return DynamicTest.dynamicTest(testFolder.name) {
            val filesWithKtExtension = testFolder.resolve("kotlin-src").walkBottomUp()
                .filter { it.isFile }
                .filter { it.extension == "kt" }
                .toList()
            val genDirectory = createTempDirectory("").toFile()
            filesWithKtExtension.forEach { file ->
                facade.generateTests(file, genDirectory)
            }
            val expectedFilesDirectory = testFolder.resolve("expected-gen")
            val expectedFiles = expectedFilesDirectory.walkBottomUp().filter { it.isFile }
            val relativePathsOfExpectedFiles =
                expectedFiles.associateWith { it.toPath().relativeTo(expectedFilesDirectory.toPath()) }
            val expectedToGeneratedMapping =
                relativePathsOfExpectedFiles.toList().associate { (generatedFile, relativePath) ->
                    generatedFile to genDirectory.toPath().resolve(relativePath).toFile()
                }
            for ((expectedFile, generatedFile) in expectedToGeneratedMapping) {
                val relativePath = expectedFile.relativeTo(expectedFilesDirectory).toPath()
                if (!generatedFile.exists() || !generatedFile.isFile) {
                    throw IllegalArgumentException("Expected file $relativePath to be generated, but it was not.")
                } else {
                    if (!treeComparer.compare(createKtFile(expectedFile), createKtFile(generatedFile))) {
                        throw IllegalArgumentException("File $relativePath was improperly generated")
                    }
                }
            }
        }
    }
}