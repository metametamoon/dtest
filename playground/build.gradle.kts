plugins {
    kotlin("jvm") version "1.6.10"
    application
    id("dtest-plugin") version "1.0"
}

group = "com.github.metametamoon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
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
    dtestImplementation("junit:junit:4.12")
}

val integrationTest = task<Test>("runDtests") {
    description = "Runs dtests."
    group = "verification"

    testClassesDirs = sourceSets["dtest"].output.classesDirs
    classpath = sourceSets["dtest"].runtimeClasspath
    shouldRunAfter("test")
    useJUnitPlatform()
}

tasks.check { dependsOn(integrationTest) }
