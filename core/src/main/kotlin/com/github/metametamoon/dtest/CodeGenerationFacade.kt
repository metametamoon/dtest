package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.extraction.ExtractedDocs
import com.github.metametamoon.dtest.extraction.extractBaseTestClass
import com.github.metametamoon.dtest.extraction.extractDocs
import com.github.metametamoon.dtest.extraction.snippets.CodeSnippet
import com.github.metametamoon.dtest.extraction.snippets.MarkdownSnippetExtractor
import com.github.metametamoon.dtest.extraction.snippets.textWithoutAsterisks
import com.github.metametamoon.dtest.generation.generateTestFile
import com.github.metametamoon.dtest.util.DtestSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

/**
 * Tests from a single test unit belong to the same class.
 */
data class TestUnit(
    val testedObjectName: String, val testSnippets: List<CodeSnippet>
)

object FileUtils {
    /**
     * Returns a **folder** specified by [fqName]
     */
    fun resolveByFqName(rootFolder: File, fqName: FqName): File {
        val segments = fqName.pathSegments()
        return segments.fold(rootFolder) { file, nextSegment -> file.resolve(nextSegment.asString()) }
    }
}

/**
 *  Is a facade for working with source files and generated files.
 */
class DtestFileGenerator(
    private val settings: DtestSettings = DtestSettings(),
    private val kotlinParserProject: Project = throw IllegalArgumentException("Please provide an argument")
) {
    fun generateTests(file: File, generatedFilesFolder: File) {
        val ktFile = createKtFile(file)
        generateTests(ktFile, generatedFilesFolder)
    }

    private fun generateTests(ktFile: KtFile, generatedFilesFolder: File) {
        setIdeaIoUseFallback()
        val extractedDocs: ExtractedDocs = extractDocs(ktFile).value
        val testUnits = extractTestUnits(extractedDocs)
        val packageFqName = ktFile.packageFqName
        val extractedBaseTestClass = extractBaseTestClass(ktFile)
        if (testUnits.isNotEmpty()) {
            val fileGenerated =
                generateTestFile(testUnits, packageFqName, settings, extractedBaseTestClass, ktFile.name)
            val folderForGeneratedFile: File = FileUtils.resolveByFqName(generatedFilesFolder, packageFqName)
            placeFile(fileGenerated, folderForGeneratedFile, ktFile.name)
        }
    }


    private fun createKtFile(
        file: File
    ): KtFile {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file) ?: throw IllegalArgumentException(
            "File ${file.absolutePath} not found"
        )
        return PsiManager.getInstance(kotlinParserProject).findFile(
            virtualFile
        ) as KtFile
    }

    private fun placeFile(fileGenerated: List<String>, folderForGeneratedFile: File, name: String) {
        val newFile = File(folderForGeneratedFile, name)
        val parentFile = newFile.parentFile
        parentFile.mkdirs()
        newFile.createNewFile()
        newFile.writeText(fileGenerated.joinToString(System.lineSeparator()))
    }


    private fun extractTestUnits(extractedDocs: ExtractedDocs) =
        extractedDocs.documentations.map { (element, documentation) ->
            val name = element.name ?: "unnamed"
            val snippets =
                MarkdownSnippetExtractor().extractCodeSnippets(documentation.textWithoutAsterisks())
            TestUnit(name, snippets)
        }.skipTestUnitsWithoutSnippets()

    private fun List<TestUnit>.skipTestUnitsWithoutSnippets() =
        filter { testUnit -> testUnit.testSnippets.isNotEmpty() }
}

