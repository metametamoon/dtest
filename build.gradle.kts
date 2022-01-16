import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.6.10-RC"
    id("idea")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
group = "me.james"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.14")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
    api("org.jetbrains.kotlin:kotlin-script-runtime")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.10")
    api("org.jetbrains.kotlin:kotlin-script-util")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable")
    testApi("org.jetbrains.kotlin:kotlin-test:kotlin-test-junit")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.6.0")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.6.0")
    implementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}