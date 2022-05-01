plugins {
    kotlin("jvm") version "1.6.10"
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.github.metametamoon"
version = "1.0"

repositories {
    mavenCentral()
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
            group = "com.github.metametamoon"
            implementationClass = "plugin.DtestPlugin"
            version = "1.0"
        }
    }
}


publishing {
    repositories {
        maven {
            rootProject.layout
            url = uri(rootProject.layout.buildDirectory.dir("repo"))
        }
    }
}

