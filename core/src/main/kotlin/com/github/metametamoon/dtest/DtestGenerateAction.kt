package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.util.DtestSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.project.stateStore
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.util.isKotlinFileType
import org.jetbrains.kotlin.psi.KtFile

fun java.io.File.toVirtualFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByIoFile(this)


class DtestGenerateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectRootPath = project.stateStore.projectBasePath
        val settings = DtestSettings.readFromFile(
            DtestJbSettings.getInstance().getSettingsFile(projectRootPath)
        ) ?: DtestSettings()
        val generationFacade = DtestFileGenerator(settings, project)
        val sourceFolder = DtestJbSettings.getInstance().getSourceFolder(projectRootPath)
            .toVirtualFile()!!
        val generationFolder = DtestJbSettings.getInstance().getGenerationFolder(projectRootPath)
            .toVirtualFile()!!
        ApplicationManager.getApplication().runWriteAction {
            generateFiles(sourceFolder, project, generationFacade, generationFolder)
        }

    }

    private fun generateFiles(
        sourceFolder: VirtualFile,
        project: Project,
        generationFacade: DtestFileGenerator,
        generationFolder: VirtualFile
    ) {
        VfsUtilCore.iterateChildrenRecursively(sourceFolder, { true }, { currentFile ->
            if (currentFile.isKotlinFileType()) {
                val ktFile = PsiManager.getInstance(project).findFile(currentFile) as? KtFile
                if (ktFile != null) {
                    generationFacade.generateTests(ktFile, generationFolder)
                }
            }
            true
        })
    }
}