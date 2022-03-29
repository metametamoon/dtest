repositories {
    mavenLocal()
    mavenCentral()
}

beforeEvaluate { println("beforeEvAL") }
afterEvaluate { println("afterEval") }

plugins {
    kotlin("jvm") version "1.6.10"
    application
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
