package functional

import DtestFileGenerator
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
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
import tree.comparer.Different
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
    ): Pair<KtFile, Document> {
        val codeString = file.readLines().joinToString("\n")
        val fileName = file.name
        val lightVirtualFile = LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
        val document = FileDocumentManager.getInstance().getDocument(lightVirtualFile)!!
        return (PsiManager.getInstance(kotlinParserProject).findFile(lightVirtualFile) as KtFile) to document
    }

    @TestFactory
    fun createTestsFromFolder(): List<DynamicTest> {
        return File("functional-tests").listFiles().orEmpty().filterNotNull().map { file ->
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
            val expectedFiles = expectedFilesDirectory.walkBottomUp().filter { it.isFile }.toList()
            val relativePathsOfExpectedFiles =
                expectedFiles.associateWith { it.toPath().relativeTo(expectedFilesDirectory.toPath()) }
            val expectedToGeneratedMapping =
                relativePathsOfExpectedFiles.toList().associate { (generatedFile, relativePath) ->
                    generatedFile to genDirectory.toPath().resolve(relativePath).toFile()
                }
            for ((expectedFile, generatedFile) in expectedToGeneratedMapping) {
                val relativePath = expectedFile.relativeTo(expectedFilesDirectory).toPath()
                require(!(!generatedFile.exists() || !generatedFile.isFile)) {
                    "Expected file $relativePath to be generated, but it was not."
                }
                val (expectedKtFile, expectedDocument) = createKtFile(expectedFile)
                val (actualKtFile, actualDocument) = createKtFile(generatedFile)
                val comparisonResult = treeComparer.compare(expectedKtFile, actualKtFile)
                if (comparisonResult is Different) {
                    println(
                        comparisonResult.toString(expectedFile, expectedDocument, generatedFile, actualDocument)
                    )
                    throw IllegalArgumentException("File $relativePath was improperly generated.")
                }
            }
            require(expectedFiles.size <= filesWithKtExtension.size) {
                "There are ${expectedFiles.size - filesWithKtExtension.size} excessive files."
            }
        }
    }
}