plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.intellij") version "1.4.0"
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
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.14")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.6.0")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:kotlin-test-junit")
    implementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.6.10")


    implementation("com.squareup:kotlinpoet:1.10.2")
}

intellij {
    version.set("2021.3.2")
}

tasks.test {
    useJUnit()
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("hello") {
            id = "plugin"
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

