plugins {
    id("com.android.application") version "7.3.0-beta05" apply false
    id("com.android.library") version "7.3.0-beta05" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
}

apply(from = "$rootDir/ci.gradle.kts")

allprojects {
    apply(from = "$rootDir/ktlint.gradle.kts")
}

tasks.register("clean").configure {
    delete("build")
}

kover {
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ)
    // coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO)
}
