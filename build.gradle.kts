plugins {
    id("com.android.application") version "7.4.0" apply false
    id("com.android.library") version "7.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

apply(from = "$rootDir/ci.gradle.kts")

allprojects {
    apply(from = "$rootDir/ktlint.gradle.kts")
}

tasks.register("clean").configure {
    delete("build")
}
