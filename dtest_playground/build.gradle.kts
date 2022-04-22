repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}


plugins {
    application
    kotlin("jvm") version "1.6.10"
    id("plugin") version "1.0"
}

group = "me.metametamoon"


dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
}


tasks.test {
    useJUnitPlatform()
}


application {
    mainClass.set("MainKt")
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "11"
    }
}

//tasks.named("compileGeneratedTestsKotlin") {
//    dependsOn(rootProject.project("dtest").tasks.getByName("publish"))
//}
