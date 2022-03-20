package plugin

//import generateTests
import generateTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import java.io.File

@Suppress("unused") // Is used in build.gradle.kts
class GreetingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val java = project.extensions
            .getByType(JavaPluginExtension::class.java)
        val sourceSets = java.sourceSets

        val generatedSourceSet: SourceSet = sourceSets.create("generatedTests") {
            it.java.srcDir("src/generated_tests/kotlin")
        }

        project.dependencies.apply {
            add("generatedTestsImplementation", "org.junit.jupiter:junit-jupiter:5.8.1")
        }

        val generatedTestsTask = project.tasks.register("generatedTests", Test::class.java) {
            it.useJUnitPlatform()
            it.group = "verification"
            it.testClassesDirs = generatedSourceSet.output.classesDirs
            it.classpath = generatedSourceSet.runtimeClasspath
        }

        project.tasks.named("test") {
            it.dependsOn(generatedTestsTask)
        }

        project.tasks.register("dtestsGenerateFiles") {
            val oneDirectory = generatedSourceSet.java.srcDirs.first()!!
            val files = oneDirectory.allFiles()
            for (file in files) {
                generateTests(file, oneDirectory)
            }
        }
    }

    private fun File.allFiles(): List<File> =
        listFiles().filter { it.isFile } +
                listFiles().filter { it.isDirectory }.flatMap { it.allFiles() }
}

