package plugin


import org.gradle.api.Plugin
import org.gradle.api.Project


@Suppress("unused") // Is used in build.gradle.kts
class DtestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("dtest", DtestExtension::class.java)
        extension.directoryForGeneration.convention(
            project.layout.projectDirectory.dir("src/dtest/kotlin/")
        )

        extension.directoryWithKotlinSource.convention(
            project.layout.projectDirectory.dir("src/main/kotlin")
        )

        project.tasks.register("dtestGenerate", DtestGenerateTask::class.java) {
            it.directoryWithKotlinSource.set(extension.directoryWithKotlinSource)
            it.directoryForGeneration.set(extension.directoryForGeneration)

        }

    }
}



