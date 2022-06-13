plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.4.0"
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

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
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.14")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("org.jetbrains.kotlin:kotlin-compiler:1.6.20")


    implementation("com.squareup:kotlinpoet:1.10.2")
}

intellij {
    version.set("2021.3.2")
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

            from(components["java"])
        }
    }
}



