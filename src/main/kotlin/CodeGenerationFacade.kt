import docs_to_tests.snippets.CodeSnippet
import docs_to_tests.snippets.MarkdownSnippetExtractor
import docs_to_tests.snippets.asText
import extractor.ExtractedDocs
import extractor.extractDocs
import generation.generateTestFile
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

@Suppress("unused")
fun generateTests(ktFile: KtFile, root: File) {
    setIdeaIoUseFallback()
    val extractedDocs: ExtractedDocs = extractDocs(ktFile).value
    val packageFqName = ktFile.packageFqName
    val testInfos = extractTestInfos(extractedDocs)
    val fileGenerated = generateTestFile(testInfos, packageFqName)
    val folderForGeneratedFile: File = findCorrespondingFolder(root)
    placeFile(fileGenerated, folderForGeneratedFile)
}

fun placeFile(fileGenerated: List<String>, folderForGeneratedFile: File) {
    TODO("Not yet implemented")
}

fun findCorrespondingFolder(root: File): File {
    TODO("Not yet implemented")
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

