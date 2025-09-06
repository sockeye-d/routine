import org.gradle.kotlin.dsl.dependencies

plugins {
    id("com.android.library") version "8.7.0"
    id("org.jetbrains.kotlin.android") version "2.1.0"
}

group = "org.fishnpotatoes.routine"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.brott.dev")
}

dependencies {
    compileOnly("com.acmerobotics.dashboard:dashboard:0.4.17") {
        exclude("org.firstinspires.ftc")
    }
    compileOnly("org.firstinspires.ftc:RobotCore:11.0.0")
    compileOnly("org.firstinspires.ftc:Hardware:11.0.0")
    compileOnly(project(":core"))
    compileOnly(project(":util"))
}

android {
    namespace = "org.fishnpotatoes.routine.ftc"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
        }
    }
}