package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.extraction.snippets.MarkdownSnippetExtractor
import com.github.metametamoon.dtest.extraction.snippets.childrenNoWhitespaces
import com.github.metametamoon.dtest.extraction.snippets.textWithoutAsterisks
import com.github.metametamoon.dtest.generation.GenerationUtils
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapBoth
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.idea.highlighter.KotlinTestRunLineMarkerContributor
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.io.File

class DtestLineContributor : RunLineMarkerContributor() {
    private val kotlinTestMarkerContributor = KotlinTestRunLineMarkerContributor()
    override fun getInfo(element: PsiElement): Info? {
        val kdocSection = element.parent
        return if (kdocSection is KDocSection && element.elementType == KDocTokens.LEADING_ASTERISK) {
            val kdoc = kdocSection.parent as? KDoc ?: return null
            val linesWithSnippets = MarkdownSnippetExtractor().getLinesWithTestStarts(kdoc.textWithoutAsterisks())
            val lineOfThis = kdocSection.childrenNoWhitespaces.filter { it.elementType == KDocTokens.LEADING_ASTERISK }
                .indexOf(element) + 1 // 1 - the first KDoc line with a KDOC_START, not asterisk
            val testNumber = linesWithSnippets.indexOf(lineOfThis)
            if (testNumber != -1) {
                val testFunctionDeclaration = findTestDeclaration(testNumber, kdoc.owner ?: return null)
                testFunctionDeclaration.mapBoth(
                    success = { foundDeclaration -> kotlinTestMarkerContributor.getInfo(foundDeclaration) },
                    failure = { errorMessage -> Info(AllIcons.Nodes.C_private, { errorMessage }) }
                )
            } else null
        } else null
    }

    private fun findTestDeclaration(testNumber: Int, ktDeclaration: KtDeclaration): Result<PsiElement, String> {
        val correspondingFile =
            ktDeclaration.containingFile.virtualFile ?: return Err("The file was not in file system")
        val generatedFolder = FileUtils.resolveByFqName(
            getFileWithGenerationFolder(
                ProjectFileIndex.getInstance(ktDeclaration.project).getContentRootForFile(correspondingFile)
            ),
            ktDeclaration.containingKtFile.packageFqName
        )
        val generatedFile = generatedFolder.resolve(ktDeclaration.containingKtFile.name)
        return if (generatedFile.exists() && generatedFile.isFile) {
            val virtualGeneratedFile = LocalFileSystem.getInstance().findFileByIoFile(generatedFile)
                ?: return Err("File ${generatedFile.canonicalPath} was not found in filesystem")
            val parsedGeneratedFile = PsiManager.getInstance(ktDeclaration.project)
                .findFile(virtualGeneratedFile) ?: return Err("File could not be parsed")
            val expectedClassName = GenerationUtils.getClassNameForNamedObject(ktDeclaration.name ?: "unnamed")
            val myClass = parsedGeneratedFile.childrenOfType<KtClass>()
                .firstOrNull { it.name == expectedClassName }
                ?: return Err("Class with name $expectedClassName was not found in file")
            val myMethod = myClass.childrenOfType<KtClassBody>()
                .firstOrNull()
                ?.childrenOfType<KtNamedFunction>()
                ?.firstOrNull { it.name == testNumber.toString() }
                ?: return Err("Method with name $testNumber was not found")
            val myMethodIdentifier = myMethod.nameIdentifier ?: return Err("Method identifier was null")
            Ok(myMethodIdentifier)
        } else Err("Generated file ${generatedFile.canonicalPath} does not exist")
    }


    /**
     * The path in settings can either be relative or absolute. We should check both variants
     */
    private fun getFileWithGenerationFolder(contentRootForFile: VirtualFile?): File {
        val generationFolder = File(DtestJbSettings.getInstance().pathToGenerationFolder)
        val projectRoot = contentRootForFile?.toNioPath()?.toFile() ?: return generationFolder
        val relativeFileIfThePathWasRelative = projectRoot.resolve(generationFolder)
        return relativeFileIfThePathWasRelative
    }
}