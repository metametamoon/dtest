plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}


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
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}
tasks.test {
    useJUnitPlatform()
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

