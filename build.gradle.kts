import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://code.lds.org/artifactory/mobile")
            credentials {
                val icsArtifactoryUsername: String by project
                val icsArtifactoryPassword: String by project
                username = icsArtifactoryUsername
                password = icsArtifactoryPassword
            }
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.27.0") // version plugin support
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION")
    }
}

allprojects {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://code.lds.org/artifactory/mobile")
            credentials {
                val icsArtifactoryUsername: String by project
                val icsArtifactoryPassword: String by project
                username = icsArtifactoryUsername
                password = icsArtifactoryPassword
            }
        }
    }
    // Gradle Dependency Check
    apply(plugin = "com.github.ben-manes.versions") // ./gradlew dependencyUpdates -Drevision=release
    val excludeVersionContaining = listOf("alpha", "eap") // example: "alpha", "beta"
    val ignoreArtifacts = listOf("material", "appcompat") // some artifacts may be OK to check for "alpha"... add these exceptions here

    tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
        resolutionStrategy {
            componentSelection {
                all {
                    if (ignoreArtifacts.contains(candidate.module).not()) {
                        val rejected = excludeVersionContaining.any { qualifier ->
                            candidate.version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                        }
                        if (rejected) {
                            reject("Release candidate")
                        }
                    }
                }
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
    delete("sqlite-android/src/main/obj")
    delete("sqlite-android/src/main/libs")
}
