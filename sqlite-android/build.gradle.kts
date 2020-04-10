import com.android.build.gradle.BaseExtension
import com.android.build.gradle.tasks.GenerateBuildConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    `maven-publish`
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
        minSdkVersion(AndroidSdk.MIN)
        targetSdkVersion(AndroidSdk.TARGET)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
        isAbortOnError = true
        disable("InvalidPackage")
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
            jniLibs.srcDir("src/main/libs")
            jni.srcDirs(emptyArray<String>()) //disable automatic ndk-build call
        }
        getByName("test") {
            java.srcDir("src/test/kotlin")
        }
    }

    // call regular ndk-build(.cmd) script from app directory
    externalNativeBuild {
        ndkBuild {
            setPath("src/main/jni/Android.mk")
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isJniDebuggable = true
            ndk {
                setAbiFilters(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
            }
        }
        getByName("release") {
            isMinifyEnabled = false
            ndk {
                setAbiFilters(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
            }
        }
    }

    ndkVersion = "21.0.6113669"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-module-name", Pom.LIBRARY_ARTIFACT_ID)
    }
}

// This prevents a BuildConfig from being created.
tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    // Android
    implementation(Deps.ANDROIDX_APPCOMPAT)

    // Test (Unit)
    testImplementation(Deps.JUNIT_5_API)
    testRuntimeOnly(Deps.JUNIT_5_ENGINE)
    testImplementation(Deps.MOCKITO)
}

// ===== TEST TASKS =====

// create JUnit reports
tasks.withType<Test> {
    useJUnitPlatform()
}

// ===== Maven Deploy =====

// ./gradlew clean assembleRelease publishMavenPublicationToMavenLocal
// ./gradlew clean assembleRelease publishMavenPublicationToNexusRepository
// ./gradlew clean check assembleRelease publishMavenPublicationToArtifactoryRepository

tasks.register<Jar>("sourcesJar") {
    //    from(android.sourceSets.getByName("main").java.sourceFiles)
    from(project.the<BaseExtension>().sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Pom.GROUP_ID
            artifactId = Pom.LIBRARY_ARTIFACT_ID
            version = Pom.VERSION
            artifact(tasks["sourcesJar"])
            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }

            // add dependencies to pom.xml
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                configurations.implementation.get().allDependencies.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.name)
                    dependencyNode.appendNode("version", it.version)
                }
            }
        }
    }
    repositories {
        maven {
            name = "Nexus"
            url = uri("https://code.lds.org/nexus/content/repositories/mobile-releases/")
            credentials {
                val icsNexusUsername: String? by project
                val icsNexusPassword: String? by project
                username = icsNexusUsername ?: ""
                password = icsNexusPassword ?: ""
            }
        }
        maven {
            name = "Artifactory"
            url = uri("https://code.lds.org/artifactory/mvn-mobile-releases/")
            credentials {
                val icsArtifactoryUsername: String? by project
                val icsArtifactoryPassword: String? by project
                username = icsArtifactoryUsername ?: ""
                password = icsArtifactoryPassword ?: ""
            }
        }
    }
}
