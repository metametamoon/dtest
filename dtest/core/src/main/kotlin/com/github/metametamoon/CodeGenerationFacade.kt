package com.github.metametamoon

import com.github.metametamoon.extraction.ExtractedDocs
import com.github.metametamoon.extraction.extractBaseTestClass
import com.github.metametamoon.extraction.extractDocs
import com.github.metametamoon.extraction.snippets.CodeSnippet
import com.github.metametamoon.extraction.snippets.MarkdownSnippetExtractor
import com.github.metametamoon.extraction.snippets.asText
import com.github.metametamoon.generation.generateTestFile
import com.github.metametamoon.util.DtestSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

/**
 * Tests from a single test unit belong to the same class.
 */
data class TestUnit(
    val testedObjectName: String, val testSnippets: List<CodeSnippet>
)

/**
 *  Is a facade for working with source files and generated files.
 */
class DtestFileGenerator(
    private val settings: DtestSettings = DtestSettings()
) {
    fun generateTests(file: File, generatedFilesFolder: File) {
        val ktFile = createKtFile(file, globalKotlinParserOnlyProject)
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
            val folderForGeneratedFile: File = findCorrespondingFolder(generatedFilesFolder, packageFqName)
            placeFile(fileGenerated, folderForGeneratedFile, ktFile.name)
        }
    }


    private val globalKotlinParserOnlyProject by lazy {
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
        file: File, project: Project = globalKotlinParserOnlyProject
    ): KtFile {
        val codeString = file.readLines().joinToString("\n")
        val fileName = file.name
        return PsiManager.getInstance(project).findFile(
            LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
        ) as KtFile
    }


    private fun placeFile(fileGenerated: List<String>, folderForGeneratedFile: File, name: String) {
        val newFile = File(folderForGeneratedFile, name)
        val parentFile = newFile.parentFile
        parentFile.mkdirs()
        newFile.createNewFile()
        newFile.writeText(fileGenerated.joinToString(System.lineSeparator()))
    }

    private fun findCorrespondingFolder(root: File, packageFqName: FqName): File {
        val segments = packageFqName.pathSegments()
        return segments.fold(root) { file, nextSegment -> file.resolve(nextSegment.asString()) }
    }

    private fun extractTestUnits(extractedDocs: ExtractedDocs) =
        extractedDocs.documentations.map { (element, documentation) ->
            val name = element.name ?: "unnamed"
            val snippets =
                MarkdownSnippetExtractor().extractCodeSnippets(documentation.asText())
            TestUnit(name, snippets)
        }.skipTestUnitsWithoutSnippets()

    private fun List<TestUnit>.skipTestUnitsWithoutSnippets() =
        filter { testUnit -> testUnit.testSnippets.isNotEmpty() }
}

