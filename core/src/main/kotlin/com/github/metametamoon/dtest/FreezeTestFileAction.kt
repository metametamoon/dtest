package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.extraction.snippets.MarkdownSnippetExtractor
import com.github.metametamoon.dtest.extraction.snippets.textWithoutAsterisks
import com.github.metametamoon.dtest.util.DtestSettings
import com.github.metametamoon.dtest.util.Imports
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.project.stateStore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.util.parentOfType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.*
import java.io.File
import kotlin.math.min

class FreezeTestFileAction : AnAction() {
    companion object {
        val LOG = Logger.getInstance(FreezeTestFileAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val generatedFile = e.getData(CommonDataKeys.PSI_FILE) as? KtFile ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val settingsFile = DtestJbSettings.getInstance()
            .getSettingsFile(project.stateStore.projectBasePath)
        freezeImports(generatedFile, settingsFile)
        freezeCorrespondingSourceFileDtests(generatedFile)
        Messages.showInfoMessage("Command executed!", "Me")
    }

    private fun freezeCorrespondingSourceFileDtests(generatedFile: KtFile) {
        val correspondingSourceFile: KtFile = findCorrespondingSourceFile(generatedFile).getOrElse {
            Messages.showErrorDialog(generatedFile.project, it, "Error While Looking Up Source File")
            return@freezeCorrespondingSourceFileDtests
        }
        for (testClass in generatedFile.childrenOfType<KtClass>()) {
            freezeTestsForTestClass(testClass, correspondingSourceFile)
        }
    }

    private fun freezeTestsForTestClass(testClass: KtClass, correspondingSourceFile: KtFile) {
        val memberFunctions = testClass
            .childrenOfType<KtClassBody>()
            .firstOrNull()
            ?.childrenOfType<KtNamedFunction>().orEmpty()
        val documentation: KDoc = findCorrespondingDoc(correspondingSourceFile, testClass) ?: run {
            Messages.showErrorDialog("KDoc for test class ${testClass.name} was not found", "KDoc Lookup Error")
            return@freezeTestsForTestClass
        }
        val docText = documentation.textWithoutAsterisks()
        val testRanges = MarkdownSnippetExtractor().getLineRanges(docText)
        warnOnDifferentSizes(testRanges, memberFunctions)
        val commonSize = min(testRanges.size, memberFunctions.size)
        for (i in 0 until commonSize) {
            val testRange = testRanges[i]
            val memberFunction = memberFunctions[i]
            insert(documentation, testRange, memberFunction)
        }
//        (0 until commonSize).map {}
    }


    private fun insert(documentation: KDoc, testRange: IntRange, memberFunction: KtNamedFunction) {
        val (firstChild, lastChild) = createPsiRange(documentation, testRange)
        val project = documentation.project
        val block = memberFunction.bodyBlockExpression ?: TODO("Support body expressions")
        val (firstChildToInsert: PsiElement, lastChildToInsert: PsiElement) = createInsertion(block, project)
        val firstChildPtr = SmartPointerManager.getInstance(project)
            .createSmartPsiElementPointer(firstChild)
        val firstChildToInsertPtr = SmartPointerManager.getInstance(project)
            .createSmartPsiElementPointer(firstChildToInsert)
        val lastChildToInsertPtr = SmartPointerManager.getInstance(project)
            .createSmartPsiElementPointer(lastChildToInsert)
        val runDelete = {
            if (firstChild.nextSibling != lastChild)
                documentation.getDefaultSection().deleteChildRange(firstChild.nextSibling, lastChild.prevSibling)
        }
        val runAdd = {
            val survivedFirstChild = firstChildPtr.element
            val survivedFirstChildToInsert = firstChildToInsertPtr.element
            val survivedLastChildToInsert = lastChildToInsertPtr.element
            if (survivedFirstChild != null && survivedFirstChildToInsert != null && survivedLastChildToInsert != null) {
                documentation.getDefaultSection()
                    .addRangeAfter(survivedFirstChildToInsert, survivedLastChildToInsert, survivedFirstChild)
            }
            Unit
        }
        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(project, runDelete, "Dtest Inject Psi", null)
        }
        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(project, runAdd, "Dtest Inject Psi", null)
        }

    }

    /**
     * stuff
     * stuff
     */
    private fun createInsertion(
        block: KtBlockExpression,
        project: Project
    ): Pair<PsiElement, PsiElement> {
        val documentationWithText = block.text.trim()
            .drop(1) // drop the braces
            .dropLast(1)
            .trim()
            .trimIndent()
            .split('\n')
            .map { " * $it" }
            .let { listOf("/**") + it + listOf(" *") + listOf(" */") + listOf("fun main() {}") }
            .joinToString(separator = "\n")
        val intermediateKdoc = PsiFileFactory.getInstance(project)
            .createFileFromText(KotlinLanguage.INSTANCE, documentationWithText)
            .childrenOfType<KtScript>().first()
            .childrenOfType<KtBlockExpression>().first()
            .childrenOfType<KtNamedFunction>().first()
            .docComment!!
        val lastWhiteSpace = run {
            var result = intermediateKdoc.getDefaultSection().lastChild
            while (!result.text.startsWith("\n"))
                result = result.prevSibling
            result
        }
        val firstChild = intermediateKdoc.getDefaultSection().firstChild
        return firstChild to lastWhiteSpace
    }

    private fun createPsiRange(documentation: KDoc, testRange: IntRange): Pair<PsiElement, PsiElement> {
        val firstIndex = testRange.first - 1 // the 0-th leading asterisk corresponds to the first line of kdoc
        val lastIndex = testRange.last - 1
        val asterisks =
            documentation.getDefaultSection().childrenOfType<PsiElement>()
                .filter { it.elementType == KDocTokens.LEADING_ASTERISK }
        return asterisks[firstIndex + 1].prevSibling to asterisks[lastIndex]
    }

    private fun warnOnDifferentSizes(
        testRanges: List<IntRange>,
        memberFunctions: List<KtNamedFunction>
    ) {
        if (testRanges.size != memberFunctions.size) {
            LOG.warn(
                "Expected to have the same amount of dtests and generated tests, " +
                        "but got:  ${testRanges.size} != ${memberFunctions.size}"
            )
        }
    }

    private fun findCorrespondingDoc(correspondingSourceFile: KtFile, testClass: KtClass): KDoc? {
        var result: KDoc? = null
        val psiFinderVisitor = object : KtTreeVisitorVoid() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                val resolvedDtest = NavigationUtil.resolveDtest(element)
                if (resolvedDtest is Ok<PsiElement>) {
                    val resolvedIdentifier = resolvedDtest.value
                    val resolvedTestClass = resolvedIdentifier.findParentOfType<KtClass>(true)
                    if (resolvedTestClass == testClass) {
                        if (result == null) {
                            result = element.parentOfType<KDoc>()
                        }
                    }
                }
            }
        }
        correspondingSourceFile.accept(psiFinderVisitor)
        return result
    }

    private fun findCorrespondingSourceFile(generatedFile: KtFile): Result<KtFile, String> {
        val sourceFolder = DtestJbSettings.getInstance().getSourceFolder(
            generatedFile.project.stateStore.projectBasePath
        )
        val resultFile = FileUtils.resolveByFqName(sourceFolder, generatedFile.packageFqName)
            .resolve(generatedFile.name)
        val virtualResultFile = LocalFileSystem.getInstance().findFileByIoFile(resultFile)
            ?: return Err("Virtual file  for file ${resultFile.canonicalPath} not found.")
        val ktFile = PsiManager.getInstance(generatedFile.project).findFile(virtualResultFile) as? KtFile
            ?: return Err("Parsing of the virtual file $virtualResultFile failed.")
        return Ok(ktFile)
    }

    private fun freezeImports(generatedFile: KtFile, settingsFile: File) {
        val newImportEntries = generatedFile.importDirectives.map { importDirective ->
            importDirective.importedFqName?.asString().orEmpty()
        }
        val newSettings = updateSettings(settingsFile, generatedFile, newImportEntries)
        settingsFile.writeText(Json.encodeToString(newSettings))
    }

    private fun updateSettings(
        settingsFile: File,
        file: KtFile,
        newImportEntries: List<String>
    ): DtestSettings {
        val settings = DtestSettings.readFromFile(settingsFile) ?: DtestSettings()
        val newImports = settings.imports.toMutableMap()
        newImports[file.packageFqName.asString() + ".${file.name}"] = Imports(true, newImportEntries)
        val newSettings = settings.copy(imports = newImports)
        return newSettings
    }
}