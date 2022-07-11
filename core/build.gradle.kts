plugins {
    id("org.jetbrains.intellij") version "1.7.0"
    kotlin("jvm") // version 1.6.20
}

intellij {
    version.set("2022.1")
    type.set("IC")
    plugins.set(listOf("org.jetbrains.kotlin", "com.intellij.gradle"))
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "11"
    }
}


tasks {
    buildSearchableOptions {
        enabled = false
    }
}


tasks.test {
    useJUnitPlatform()
}



