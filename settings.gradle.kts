rootProject.name = "dtest"

plugins {
    kotlin("jvm") version "1.6.20" apply false
}


include("core")
include("gradle-plugin")
//include("intellij-plugin")