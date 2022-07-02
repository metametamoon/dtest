plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm")
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}


dependencies {
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.14")
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.6.20")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}



tasks.shadowJar {
    archiveClassifier.set("")
    from(sourceSets.main.get().allSource)
}

val relocateShadowJar by tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.named("shadowJar").get() as com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar?
    prefix = "shadow"
}

tasks.named("shadowJar") {
    dependsOn(relocateShadowJar)
}

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

            project.shadow.component(this)

        }
    }
}



