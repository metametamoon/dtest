package plugin


import generateTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import java.io.File

@Suppress("unused") // Is used in build.gradle.kts
class GreetingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val java = project.extensions.getByType(JavaPluginExtension::class.java)
        val sourceSets = java.sourceSets

        val generatedSourceSet = sourceSets.create("generatedTests") {
        }

        generatedSourceSet.compileClasspath += sourceSets.named("main").orNull?.output!!
        generatedSourceSet.runtimeClasspath += sourceSets.named("main").orNull?.output!!

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
            val generatedSrcsDirectory = generatedSourceSet.java.srcDirs.first()!!
            println(generatedSrcsDirectory)
            val mainSrcDirectory = java.sourceSets.named("main").orNull
                ?: throw InternalError("No main module!")

            val files = mainSrcDirectory.java.sourceDirectories.first()!!.allFiles()
            println("main is ${mainSrcDirectory.java.sourceDirectories.files} with files ${files}")
            for (file in files) {
                generateTests(file, generatedSrcsDirectory)
            }
        }
    }

    private fun File.allFiles(): List<File> =
        (listFiles() ?: arrayOf()).filter { it.isFile } +
                (listFiles() ?: arrayOf()).filter { it.isDirectory }.flatMap { it.allFiles() }
}

