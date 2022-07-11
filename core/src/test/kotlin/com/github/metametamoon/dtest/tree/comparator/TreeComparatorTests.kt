package com.github.metametamoon.dtest.tree.comparator

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.loadHeadlessAppInUnitTestMode
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.io.File

class TreeComparatorTests {
    private val kotlinParserProject = run {
        loadHeadlessAppInUnitTestMode()
        ProjectManagerEx.getInstanceEx().newProject(
            File("").toPath(), OpenProjectTask(
                isNewProject = true,
            )
        )!!
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

    private fun compareFilesInFolder(folder: File): TreeComparisonResult {
        val (expectedFile, _) = createKtFile(folder.resolve("expected.kt"))
        val (actualFile, _) = createKtFile(folder.resolve("actual.kt"))
        return TreeComparator().compare(expectedFile, actualFile)
    }

    private val treeComparisonDirectory = File("tree-comparison")

    @Test
    fun `compare identical trees`() {
        checkComparedSame("identical")
    }

    @Test
    fun `compare differing only in spaces trees`() {
        checkComparedSame("diff-in-spaces")
    }

    @Test
    fun `compare different trees`() {
        checkComparedDifferent("different")
    }

    @Test
    fun `ignore public modifiers on functions`() {
        checkComparedSame("ignore-public-access-modifier")
    }

    @Test
    fun `ignore public modifiers on functions on all depth levels`() {
        checkComparedSame("ignore-public-access-modifier-deep")
    }

    @Test
    fun `private modifier makes the tree different`() {
        checkComparedDifferent("private-modifier")
    }

    @Test
    fun `unit return type can be omitted`() {
        checkComparedSame("unit-return-type")
    }

    @Test
    fun `non-unit return type cannot be omitted`() {
        checkComparedDifferent("non-unit-return-type")
    }

    @Test
    fun `different imports should matter`() {
        checkComparedDifferent("different-imports")
    }

    @Test
    fun `additional imports are bad`() {
        checkComparedDifferent("expected-without-imports")
    }

    @Test
    fun `class comparison ignores public modifiers and empty primary ctor`() {
        checkComparedSame("class-comparison")
    }

    private fun checkComparedDifferent(subfolderName: String) {
        val folder = treeComparisonDirectory.resolve(subfolderName)
        val expectedFile = folder.resolve("expected.kt")
        val (expectedKtFile, expectedDocument) = createKtFile(expectedFile)

        val actualFile = folder.resolve("actual.kt")
        val (actualKtFile, actualDocument) = createKtFile(actualFile)

        val comparisonResult = TreeComparator().compare(expectedKtFile, actualKtFile)
        if (comparisonResult is Different) {
            println(comparisonResult.toString(expectedFile, expectedDocument, actualFile, actualDocument))
        }
        assertFalse(comparisonResult.areSame())
    }

    private fun checkComparedSame(subfolderName: String) {
        val comparisonResult = compareFilesInFolder(treeComparisonDirectory.resolve(subfolderName))
        if (!comparisonResult.areSame()) {
            throw AssertionError("Expected files to be same, but they are not:\n $comparisonResult")
        }
    }
}