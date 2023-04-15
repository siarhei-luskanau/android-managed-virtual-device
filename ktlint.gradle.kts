val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.48.2") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(
    mapOf(
        "dir" to "src",
        "include" to "**/*.kt",
        "include" to "**/*.kts"
    )
)

val ktlintCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)
    group = "ktlint"
    description = "Check Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("--android", "**/src/**/*.kt", "**/src/**/*.kts", "*.kts")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)
    group = "ktlint"
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("--android", "-F", "**/src/**/*.kt", "**/src/**/*.kts", "*.kts")
}

val applyToIDEAProject by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)
    group = "ktlint"
    description =
        "Change the code style config files to be compliant with Android Kotlin Style Guide."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("applyToIDEAProject", "-y")
}
