plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

// Kotlin Libraries targeting Java8 bytecode can cause the following error (such as okHttp 4.x):
// "Cannot inline bytecode built with JVM target 1.8 into bytecode that is being built with JVM target 1.6. Please specify proper '-jvm-target' option"
// The following is added to allow the Kotlin Compiler to compile properly
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

android {
    compileSdkVersion(AndroidSdk.COMPILE)

    defaultConfig {
        applicationId = "org.sqlite.app.customsqlite"
        minSdkVersion(AndroidSdk.MIN)
        targetSdkVersion(AndroidSdk.TARGET)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = " DEV"
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
        getByName("test") {
            java.srcDir("src/test/kotlin")
        }
    }
}

dependencies {
    implementation(project(":sqlite-android"))
    implementation(Deps.ANDROIDX_APPCOMPAT)
    implementation(Deps.KOTLIN_STD_LIB)
    implementation(Deps.COROUTINES)
    implementation(Deps.TIMBER)
}