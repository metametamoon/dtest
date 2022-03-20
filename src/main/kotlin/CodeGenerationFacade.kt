import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import docs_to_tests.snippets.CodeSnippet
import docs_to_tests.snippets.MarkdownSnippetExtractor
import docs_to_tests.snippets.asText
import extractor.ExtractedDocs
import extractor.extractDocs
import generation.generateTestFile
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

@Suppress("unused")
fun generateTests(ktFile: KtFile, root: File) {
    setIdeaIoUseFallback()
    val extractedDocs: ExtractedDocs = extractDocs(ktFile).value
    val testInfos = extractTestInfos(extractedDocs)
    val packageFqName = ktFile.packageFqName
    val fileGenerated = generateTestFile(testInfos, packageFqName)
    val folderForGeneratedFile: File = findCorrespondingFolder(root, packageFqName)
    placeFile(fileGenerated, folderForGeneratedFile, ktFile.name)
}

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

private fun createKtFile(
    file: File, project: Project
): KtFile {
    val codeString = file.readLines().joinToString("\n")
    val fileName = file.name
    return PsiManager.getInstance(project).findFile(
        LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
    ) as KtFile
}

fun generateTests(file: File, root: File) {
    val ktFile = createKtFile(file, globalKotlinParserOnlyProject)
    generateTests(ktFile, root)
}

fun placeFile(fileGenerated: List<String>, folderForGeneratedFile: File, name: String) {
    val newFile = File(folderForGeneratedFile, name)
    newFile.createNewFile()
    newFile.writeText(fileGenerated.joinToString(System.lineSeparator()))
}

fun findCorrespondingFolder(root: File, packageFqName: FqName): File {
    val segments = packageFqName.pathSegments()
    return segments.fold(root) { file, nextSegment -> file.resolve(nextSegment.asString()) }
}

private fun extractTestInfos(extractedDocs: ExtractedDocs) =
    extractedDocs.documentations.map { (element, documentation) ->
        val name = element.name ?: "unnamed"
        val snippets =
            MarkdownSnippetExtractor().extractCodeSnippets(documentation.asText())
        TestInfo(name, snippets)
    }

data class TestInfo(
    val name: String, val snippets: List<CodeSnippet>
)
