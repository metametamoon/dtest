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


val outsideShadowJar by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}


configurations.shadow {
    extendsFrom(outsideShadowJar)
}

configurations.implementation {
    extendsFrom(outsideShadowJar)
}

val outsideJarDependencies = mutableListOf<String>()
fun DependencyHandlerScope.putOutsideShadowJar(config: String) {
    outsideShadowJar(config)
    outsideJarDependencies.add(config)
}

dependencies {
    putOutsideShadowJar("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    putOutsideShadowJar("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.14")
    putOutsideShadowJar("com.squareup:kotlinpoet:1.10.2")
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.6.20")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}



tasks.shadowJar {
    archiveClassifier.set("")
    from(sourceSets.main.get().allSource)
//    dependencies {
//        for (config in outsideJarDependencies) {
//            exclude(dependency(config))
//        }
//    }
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



