package com.github.metametamoon.dtest

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.Messages
import com.intellij.project.stateStore
import com.intellij.util.io.exists
import com.intellij.util.io.inputStream
import com.intellij.util.io.isFile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlin.io.path.absolutePathString

@Suppress("PROVIDED_RUNTIME_TOO_LOW") // workaround for a bug
@Serializable
data class DtestJbSettingsHelper(
    var pathToSourceFolder: String,
    var pathToGenerationFolder: String,
    var pathToSettings: String,
)

class DtestJbSettingInitializationActivity : StartupActivity {
    @OptIn(ExperimentalSerializationApi::class)
    override fun runActivity(project: Project) {
        val root = project.stateStore.projectBasePath
        val dtestInitFile = root.resolve("dtest-init.json")
        if (dtestInitFile.exists() && dtestInitFile.isFile()) {
            val jbSettingsHelper = Json.decodeFromStream<DtestJbSettingsHelper>(dtestInitFile.inputStream())
            val globalSettings = DtestJbSettings.getInstance()
            globalSettings.pathToSettings =
                root.resolve(jbSettingsHelper.pathToSettings).absolutePathString()
            globalSettings.pathToSourceFolder =
                root.resolve(jbSettingsHelper.pathToSourceFolder).absolutePathString()
            globalSettings.pathToGenerationFolder =
                root.resolve(jbSettingsHelper.pathToGenerationFolder).absolutePathString()
            Messages.showInfoMessage(
                project,
                "Dtest file locations were initialized via ${dtestInitFile.absolutePathString()}",
                "Init"
            )
        }
    }
}