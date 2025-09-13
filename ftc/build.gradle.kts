import org.gradle.kotlin.dsl.dependencies

plugins {
    id("dev.frozenmilk.android-library") version "10.1.1-0.1.3"
}

android.namespace = "org.fishnpotatoes.routine"

ftc {
    kotlin

    sdk {
        RobotCore
        Hardware
        FtcCommon {
            configurationNames += "testImplementation"
        }
    }
}

//group = "org.fishnpotatoes.routine"
//version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.brott.dev")
}

dependencies {
    compileOnly("com.acmerobotics.dashboard:dashboard:0.4.17") {
        exclude("org.firstinspires.ftc")
    }
    //compileOnly("org.firstinspires.ftc:RobotCore:11.0.0")
    //compileOnly("org.firstinspires.ftc:Hardware:11.0.0")
    implementation(project(":core"))
    implementation(project(":util"))
}

//android {
//    namespace = "org.fishnpotatoes.routine.ftc"
//    compileSdk = 35
//
//    defaultConfig {
//        minSdk = 24
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//
//    publishing {
//        multipleVariants {
//            withSourcesJar()
//            withJavadocJar()
//            allVariants()
//        }
//    }
//}