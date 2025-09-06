plugins {
    kotlin("jvm") version "2.1.0"
}

dependencies {
    implementation(kotlin("stdlib"))
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(8)
}
