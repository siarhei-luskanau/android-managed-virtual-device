import org.apache.tools.ant.taskdefs.condition.Os

val CI_GRADLE = "CI_GRADLE"

tasks.register("ciBuildAll") {
    group = CI_GRADLE
    doLast {
        gradlew(
            "clean",
            "ktlintFormat",
            "ktlintCheck",
            "lint",
            "koverVerify",
            "koverReport",
            "koverMergedReport",
            "assembleDebug",
        )
    }
}

tasks.register("ciEmulatorAll") {
    group = CI_GRADLE
    doLast {
        listOf(27, 28, 29, 30, 31, 32, 33).forEach { apiLevelIt ->
            listOf("google", "google-atd", "aosp", "aosp-atd").forEach { systemImageSourceIt ->
                listOf("", "64Bit").forEach { require64BitVariant ->
                    val name = listOf(
                        "managedVirtualDevice",
                        apiLevelIt.toString(),
                        systemImageSourceIt.capitalize(),
                        require64BitVariant.capitalize()
                    ).joinToString(separator = "")
                    try {
                        gradlew("${name}Check")
                    } catch (error: Throwable) {
                        Error(name, error)
                    }
                }
            }
        }
    }
}

fun gradlew(
    vararg tasks: String,
    addToEnvironment: Map<String, String>? = null
) {
    exec {
        executable = File(
            project.rootDir,
            if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew",
        )
            .also { it.setExecutable(true) }
            .absolutePath
        args = mutableListOf<String>().apply {
            addAll(tasks)
            add("--stacktrace")
        }
        addToEnvironment?.let {
            environment = environment.toMutableMap().apply { putAll(it) }
        }
        println("commandLine: ${this.commandLine}")
    }.apply { println("ExecResult: $this") }
}
