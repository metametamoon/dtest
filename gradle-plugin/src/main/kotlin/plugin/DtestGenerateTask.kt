package plugin

import com.github.metametamoon.dtest.DtestFileGenerator
import com.github.metametamoon.dtest.util.DtestSettings
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileType
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import kotlin.io.path.Path
import kotlin.io.path.relativeToOrNull

abstract class DtestGenerateTask : DefaultTask() {
    @get:Incremental
    @get:InputDirectory
    abstract val directoryWithKotlinSource: DirectoryProperty

    @get:OutputDirectory
    abstract val directoryForGeneration: DirectoryProperty

    @get:Input
    abstract val defaultTestAnnotationFqName: Property<String>

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val facade = DtestFileGenerator(DtestSettings(defaultTestAnnotationFqName = defaultTestAnnotationFqName.get()))
        inputChanges.getFileChanges(directoryWithKotlinSource)
            .filter { it.fileType == FileType.FILE }
            .filter { it.file.extension == "kt" }
            .forEach {
                if (it.changeType == ChangeType.ADDED || it.changeType == ChangeType.MODIFIED)
                    facade.generateTests(it.file, directoryForGeneration.get().asFile)
                else {
                    val relativePath = Path(it.file.absolutePath)
                        .relativeToOrNull(Path(directoryWithKotlinSource.asFile.get().absolutePath)) ?: run {
                        println("Not relative")
                        return@forEach
                    }
                    val expectedOutputFile = Path(directoryForGeneration.asFile.get().absolutePath)
                        .resolve(relativePath)
                        .toFile()
                    if (expectedOutputFile.exists() && expectedOutputFile.isFile) {
                        println("Deleting ${expectedOutputFile.name} because it is produced from a deleted file ${it.file.name}")
                        expectedOutputFile.delete()
                    }

                }
            }
    }

}
