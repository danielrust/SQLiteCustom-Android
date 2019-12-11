const val KOTLIN_VERSION = "1.3.61"

object Deps {
    const val KOTLIN_STD_LIB = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KOTLIN_VERSION"
    private const val COROUTINES_VERSION = "1.3.2"
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$COROUTINES_VERSION"

    // Android
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:1.1.0"

    // Code
    const val TIMBER = "com.jakewharton.timber:timber:4.7.1"

    // Testing
    const val JUNIT_5_API = "org.junit.jupiter:junit-jupiter-api:5.5.2"
    const val JUNIT_5_ENGINE = "org.junit.jupiter:junit-jupiter-engine:5.5.2"
    const val MOCKITO = "org.mockito:mockito-core:3.2.0"
}