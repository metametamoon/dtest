plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

gradlePlugin {
    plugins {
        create("hello") {
            id = "plugin"
            implementationClass = "plugin.GreetingPlugin"
            version = "1.0"
        }
    }
}

//publishing {
//    repositories {
//        maven {
//            url = uri(layout.projectDirectory.dir("../buildPlugin"))
//        }
//    }
//}