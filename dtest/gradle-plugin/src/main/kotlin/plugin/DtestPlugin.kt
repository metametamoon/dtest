package plugin


import generateTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File


@Suppress("unused") // Is used in build.gradle.kts
class DtestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("dtest", DtestExtension::class.java)
        extension.directoryForGeneration.convention {
            project.layout.projectDirectory.dir("src/dtest/kotlin/").asFile
        }
        extension.directoryWithKotlinSource.convention {
            project.layout.projectDirectory.dir("src/main/kotlin").asFile
        }

        project.tasks.register("dtestGenerate") {
            val ktFiles = extension.directoryWithKotlinSource.get().asFile.allFiles()
                .filter { it.extension == "kt" }
            for (ktFile in ktFiles) {
                generateTests(ktFile, extension.directoryForGeneration.get().asFile)
            }
        }
    }
}

private fun File.allFiles(): List<File> =
    (listFiles() ?: arrayOf()).filter { it.isFile } +
            (listFiles() ?: arrayOf()).filter { it.isDirectory }.flatMap(File::allFiles)



