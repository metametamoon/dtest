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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import tree.comparer.Different
import tree.comparer.TreeComparer
import util.DtestSettings
import java.io.File
import java.nio.file.Path
import kotlin.io.path.relativeTo
import kotlin.random.Random

class FunctionalTests {
    @TestFactory
    fun createTestsFromFolder(): List<DynamicTest> {
        return File("functional-tests").listFiles().orEmpty().filterNotNull().map { file ->
            generateFunctionalTest(file)
        }
    }

    @Test
    fun `single-file-with-many-tests-on-function`() {
        File("functional-tests").resolve("single-file-with-many-tests-on-function").let { file ->
            generateFunctionalTest(file).executable.execute()
        }
    }

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

    private val treeComparer = TreeComparer()

    data class GeneratedFile(
        val file: File,
        val relativePath: Path
    )

    private fun generateFunctionalTest(testFolder: File): DynamicTest {
        val settings = DtestSettings.readFromFile(testFolder.resolve("settings.json"))
            ?: DtestSettings(defaultTestAnnotationFqName = "kotlin.test.Test")
        val facade = DtestFileGenerator(settings)
        return DynamicTest.dynamicTest(testFolder.name) {
            val sourceFiles = testFolder.resolve("kotlin-src").getFilesWithKtExtension()
            val genDirectory = createTemporaryDirectory()
            println("Generation folder for test ${testFolder.name} is ${genDirectory.name}")
            sourceFiles.forEach { file ->
                facade.generateTests(file, genDirectory)
            }
            val expectedFilesDirectory = testFolder.resolve("expected-gen")
            val expectedFiles = expectedFilesDirectory.getFilesWithKtExtension()
            val expectedToGeneratedMapping =
                getExpectedToGeneratedMapping(expectedFiles, expectedFilesDirectory, genDirectory)
            compareExpectedFilesWithGenerated(expectedToGeneratedMapping)
            checkAmountOfGeneratedFiles(expectedFiles, genDirectory)
        }
    }

    private fun checkAmountOfGeneratedFiles(
        expectedFiles: List<File>,
        genDirectory: File,
    ) {
        val generatedFilesCount = genDirectory.getFilesWithKtExtension()
            .toList().size
        require(expectedFiles.size <= generatedFilesCount) {
            "There are ${expectedFiles.size - generatedFilesCount} excessive files."
        }
    }

    private fun File.getFilesWithKtExtension(): List<File> = walkBottomUp()
        .filter { it.isFile }
        .filter { it.extension == "kt" }
        .toList()

    private fun compareExpectedFilesWithGenerated(expectedToGeneratedMapping: Map<File, GeneratedFile>) {
        for ((expectedFile, generatedFile) in expectedToGeneratedMapping) {
            require(generatedFile.file.exists() && generatedFile.file.isFile) {
                "Expected file ${generatedFile.relativePath} to be generated, but it was not."
            }
            compareFiles(expectedFile, generatedFile.file, generatedFile.relativePath)
        }
    }

    private fun getExpectedToGeneratedMapping(
        expectedFiles: List<File>,
        expectedFilesDirectory: File,
        genDirectory: File
    ): Map<File, GeneratedFile> {
        val relativePathsOfExpectedFiles =
            expectedFiles.associateWith { it.toPath().relativeTo(expectedFilesDirectory.toPath()) }
        val expectedToGeneratedMapping =
            relativePathsOfExpectedFiles.toList().associate { (generatedFile, relativePath) ->
                generatedFile to GeneratedFile(genDirectory.toPath().resolve(relativePath).toFile(), relativePath)
            }
        return expectedToGeneratedMapping
    }

    private fun compareFiles(
        expectedFile: File,
        generatedFile: File,
        relativePathForExceptionMessage: Path
    ) {
        val (expectedKtFile, expectedDocument) = createKtFile(expectedFile)
        val (actualKtFile, actualDocument) = createKtFile(generatedFile)
        val comparisonResult = treeComparer.compare(expectedKtFile, actualKtFile)
        if (comparisonResult is Different) {
            println(
                comparisonResult.toString(expectedFile, expectedDocument, generatedFile, actualDocument)
            )
            throw IllegalArgumentException("File $relativePathForExceptionMessage was improperly generated.")
        }
    }

    private fun createTemporaryDirectory(): File {
        while (true) {
            val rnd = Random.Default
            val name = rnd.nextInt(100_000, 999_999).toString()
            val file = File("tmp").resolve(name)
            if (!file.exists()) {
                file.mkdirs()
                return file
            }
        }
    }
}