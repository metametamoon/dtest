package com.github.metametamoon.dtest

import com.intellij.ide.util.PropertiesComponent
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KProperty

object PropertyComponentDelegate {
    private const val dtestPrefix = "com.github.metametamoon.dtest"
    operator fun getValue(thisRef: Any, property: KProperty<*>): String {
        val lookupName = dtestPrefix + property.name
        return PropertiesComponent.getInstance().getValue(lookupName).orEmpty()
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        val lookupName = dtestPrefix + property.name
        PropertiesComponent.getInstance().setValue(lookupName, value)
    }
}

class DtestJbSettings private constructor(
) {
    var pathToSourceFolder: String by PropertyComponentDelegate
    var pathToGenerationFolder: String by PropertyComponentDelegate
    var pathToSettings: String by PropertyComponentDelegate

    /**
     * The path in settings can either be relative or absolute. We should check both variants
     */
    fun getFileWithGenerationFolder(projectRootPath: Path): File =
        getFileOrRelative(projectRootPath, pathToGenerationFolder)

    fun getSettingsFile(projectRootPath: Path): File = getFileOrRelative(projectRootPath, pathToSettings)

    fun getSourceFolder(projectRootPath: Path): File = getFileOrRelative(projectRootPath, pathToSourceFolder)

    private fun getFileOrRelative(rootPath: Path, path: String): File {
        val file = File(path)
        val projectRoot = rootPath.toFile() ?: return file
        val relativeFileIfThePathWasRelative = projectRoot.resolve(file)
        return relativeFileIfThePathWasRelative
    }

    companion object {
        private val instance = DtestJbSettings()
        fun getInstance(): DtestJbSettings = instance
    }
}