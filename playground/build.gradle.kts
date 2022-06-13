plugins {
    kotlin("jvm") version "1.6.20"
    id("dtest-plugin") version "1.0"
    application
}

group = "com.github.metametamoon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}
sourceSets {
    create("dtest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        java.srcDir("src/dtest/kotlin")
    }
}

val dtestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["dtestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    dtestImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    configurations["dtestRuntimeOnly"]("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

val integrationTest = task<Test>("runDtests") {
    description = "Runs dtests."
    group = "verification"

    testClassesDirs = sourceSets["dtest"].output.classesDirs
    classpath = sourceSets["dtest"].runtimeClasspath
    shouldRunAfter("test")
    useJUnit()
    useJUnitPlatform()
}

tasks.check { dependsOn(integrationTest) }
