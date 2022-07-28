package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.extraction.snippets.MarkdownSnippetExtractor
import com.github.metametamoon.dtest.extraction.snippets.childrenNoWhitespaces
import com.github.metametamoon.dtest.extraction.snippets.textWithoutAsterisks
import com.github.metametamoon.dtest.generation.GenerationUtils
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.project.stateStore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction

data class DtestDeclaration(
    val testNumber: Int,
    val sourceDeclaration: KtDeclaration
)

class NavigationUtil {
    companion object {
        /**
         * @return `Ok(test method identifier)` if the dtest resolved successfully, os `Err(messageString)`
         * if there was an error while resolving the dtest, or `null`, if the [element] is not a
         * `KDOC_LEADING_STAR` on a line with the start of the dtest.
         */
        fun resolveDtest(element: PsiElement): Result<PsiElement, String>? {
            val dtestDeclaration = lazyResolveDtest(element)
            return if (dtestDeclaration != null)
                findTestDeclaration(dtestDeclaration.testNumber, dtestDeclaration.sourceDeclaration)
            else
                null
        }

        /**
         * @return `null` if element is not KDOC_LEADING_STAR on the line with some kdoc
         */
        fun lazyResolveDtest(element: PsiElement): DtestDeclaration? {
            val kdocSection = element.parent
            return if (kdocSection is KDocSection && element.elementType == KDocTokens.LEADING_ASTERISK) {
                val kdoc = kdocSection.parent as? KDoc ?: return null
                val linesWithSnippets = MarkdownSnippetExtractor().getLinesWithTestStarts(kdoc.textWithoutAsterisks())
                val lineOfThis =
                    kdocSection.childrenNoWhitespaces.filter { it.elementType == KDocTokens.LEADING_ASTERISK }
                        .indexOf(element) + 1 // 1 - the first KDoc line with a KDOC_START, not asterisk
                val testNumber = linesWithSnippets.indexOf(lineOfThis)
                if (testNumber != -1) {
                    DtestDeclaration(testNumber, kdoc.owner ?: return null)
                } else null
            } else null

        }

        private fun findTestDeclaration(testNumber: Int, sourceDeclaration: KtDeclaration): Result<PsiElement, String> {
            val generatedFolderRoot = DtestJbSettings.getInstance()
                .getGenerationFolder(sourceDeclaration.project.stateStore.projectBasePath)
            val generatedFolder = FileUtils.resolveByFqName(
                generatedFolderRoot,
                sourceDeclaration.containingKtFile.packageFqName
            )
            val generatedFile = generatedFolder.resolve(sourceDeclaration.containingKtFile.name)
            return if (generatedFile.exists() && generatedFile.isFile) {
                val virtualGeneratedFile = LocalFileSystem.getInstance().findFileByIoFile(generatedFile)
                    ?: return Err("File ${generatedFile.canonicalPath} was not found in filesystem")
                val parsedGeneratedFile = PsiManager.getInstance(sourceDeclaration.project)
                    .findFile(virtualGeneratedFile) ?: return Err("File could not be parsed")
                val expectedClassName = GenerationUtils.getClassNameForNamedObject(sourceDeclaration.name ?: "unnamed")
                val myClass = parsedGeneratedFile.childrenOfType<KtClass>()
                    .firstOrNull { it.name == expectedClassName }
                    ?: return Err("Class with name $expectedClassName was not found in file")
                val myMethod = myClass.childrenOfType<KtClassBody>()
                    .firstOrNull()
                    ?.childrenOfType<KtNamedFunction>()
                    ?.firstOrNull { it.name == GenerationUtils.getMethodNameForIndex(testNumber) }
                    ?: return Err("Method with name $testNumber was not found")
                val myMethodIdentifier = myMethod.nameIdentifier ?: return Err("Method identifier was null")
                Ok(myMethodIdentifier)
            } else Err("Generated file ${generatedFile.canonicalPath} does not exist")
        }
    }
}