plugins {
    id("com.android.application") version "7.2.0-beta04" apply false
    id("com.android.library") version "7.2.0-beta04" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

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
