package plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class DtestExtension {
    abstract val directoryForGeneration: DirectoryProperty
    abstract val directoryWithKotlinSource: DirectoryProperty
    abstract val defaultTestAnnotationFqName: Property<String>
}