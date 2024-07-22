plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.detekt)
}

apply(from = "$rootDir/ci.gradle.kts")

allprojects {
    apply(from = "$rootDir/ktlint.gradle")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        verify {
            rule {
                minBound(95)
                maxBound(98)
            }
        }
    }
}

subprojects {
    dependencies {
        kover(project(path))
    }
}

tasks.register("clean").configure {
    delete("build")
}
