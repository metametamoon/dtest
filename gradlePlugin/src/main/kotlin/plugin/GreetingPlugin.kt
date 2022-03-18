package plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension


class GreetingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("myTask") {
            println("Hello from here!")
            try {
                val javaPlugin = project.extensions
                    .getByType(JavaPluginExtension::class.java)
                println("Java extension found!: ${javaPlugin.sourceSets.asMap}")
            } catch (e: Throwable) {
                println("Error: $e")
            }
        }

    }
}