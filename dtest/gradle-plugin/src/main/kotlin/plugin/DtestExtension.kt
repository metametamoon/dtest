package plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory

abstract class DtestExtension {
    @get:OutputDirectory
    abstract val directoryForGeneration: DirectoryProperty

    @get: InputDirectory
    abstract val directoryWithKotlinSource: DirectoryProperty
}