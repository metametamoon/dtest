package com.github.metametamoon.functional

import com.github.metametamoon.DtestFileGenerator
import com.github.metametamoon.tree.comparator.Different
import com.github.metametamoon.tree.comparator.TreeComparator
import com.github.metametamoon.util.DtestSettings
import com.intellij.ide.impl.NewProjectUtil
import com.intellij.ide.util.newProjectWizard.AbstractProjectWizard
import com.intellij.ide.util.newProjectWizard.StepSequence
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
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
    fun `multiple-files-within-packages`() {
        File("functional-tests").resolve("multiple-files-within-packages").let { file ->
            generateFunctionalTest(file).executable.execute()
        }
    }

    private val kotlinParserProject = run {
        NewProjectUtil.createFromWizard(object : AbstractProjectWizard("Wizard", null, "") {
            override fun getSequence(): StepSequence {
                return StepSequence()
            }
        })
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

    private val treeComparator = TreeComparator()

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
        val comparisonResult = treeComparator.compare(expectedKtFile, actualKtFile)
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