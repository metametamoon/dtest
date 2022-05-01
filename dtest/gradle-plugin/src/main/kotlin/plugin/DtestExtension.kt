package plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory

abstract class DtestExtension {
    @get:OutputDirectory
    abstract val directoryForGeneration: RegularFileProperty

    @get: InputDirectory
    abstract val directoryWithKotlinSource: RegularFileProperty
}