package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.util.DtestSettings
import com.github.metametamoon.dtest.util.Imports
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.project.stateStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

class LoadToSettingsFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val sourceFile = e.getData(CommonDataKeys.PSI_FILE) as? KtFile ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val settingsFile = DtestJbSettings.getInstance()
            .getSettingsFile(project.stateStore.projectBasePath)
        val newImportEntries = sourceFile.importDirectives.map { importDirective ->
            importDirective.importedFqName?.asString().orEmpty()
        }
        val newSettings = updateSettings(settingsFile, sourceFile, newImportEntries)
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