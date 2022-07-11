plugins {
    id("org.jetbrains.intellij") version "1.7.0"
    kotlin("jvm") // version 1.6.20
    kotlin("plugin.serialization") version "1.6.20"
    `maven-publish`
}

intellij {
    version.set("2022.1")
    type.set("IC")
    plugins.set(listOf("org.jetbrains.kotlin", "com.intellij.gradle"))
}

dependencies {
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.16")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.16")
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

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

dependencies {
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.14")
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}


//tasks.shadowJar {
//    archiveClassifier.set("")
//    from(sourceSets.main.get().allSource)
//}
//
//val relocateShadowJar by tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation>("relocateShadowJar") {
//    target = tasks.named("shadowJar").get() as com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar?
//    prefix = "shadow"
//}
//
//tasks.named("shadowJar") {
//    dependsOn(relocateShadowJar)
//}

tasks.withType<Zip>().configureEach {
    this.isZip64 = true
}


tasks.test {
    useJUnitPlatform()
    workingDir = project.layout.projectDirectory.asFile.resolve("testData")
}

publishing {
    repositories {
        maven {
            url = uri(rootProject.layout.buildDirectory.dir("repo"))
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String?
            artifactId = "core"
            version = rootProject.version as String?
            from(components["java"])
//            project.shadow.component(this)

        }
    }
}



