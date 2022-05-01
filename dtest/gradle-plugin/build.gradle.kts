plugins {
    kotlin("jvm") version "1.6.10"
    `java-gradle-plugin`
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://www.jetbrains.com/intellij-repository/releases")
    }
    maven {
        url =
            uri("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }
}

dependencies {
    implementation(project(":core"))
}



gradlePlugin {
    plugins {
        create("DtestPlugin") {
            id = "dtest-plugin"
            group = rootProject.group
            implementationClass = "plugin.DtestPlugin"
            version = "1.0"
        }
    }
}


publishing {
    repositories {
        maven {
            url = uri(rootProject.layout.buildDirectory.dir("repo"))
        }
    }
}

