import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm").apply(true)
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(compose.desktop.currentOs)
    implementation(project(":core"))
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvmToolchain(23)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "routine-test"
            packageVersion = "1.0.0"
        }
    }
}
