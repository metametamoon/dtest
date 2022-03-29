pluginManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("build/repo/")
        }
        gradlePluginPortal()
    }

}
rootProject.name = "dtest"

include("dtest_playground")
include("dtest")