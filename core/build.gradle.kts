plugins {
    kotlin("jvm") version "2.1.0"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":util"))
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(8)
}
