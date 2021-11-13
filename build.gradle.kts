plugins {
    id("com.android.application") version "7.0.3" apply false
    id("com.android.library") version "7.0.3" apply false
    id("org.jetbrains.kotlin.android") version "1.5.31" apply false
}

allprojects {
    apply(from = "$rootDir/ktlint.gradle.kts")
}

tasks.register("clean").configure {
    delete("build")
}
