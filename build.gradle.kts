plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinx.kover)
}

apply(from = "$rootDir/ci.gradle.kts")

allprojects {
    apply(from = "$rootDir/ktlint.gradle.kts")
    apply(plugin = "io.gitlab.arturbosch.detekt")
}

tasks.register("clean").configure {
    delete("build")
}
