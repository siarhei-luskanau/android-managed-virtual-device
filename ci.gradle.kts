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
            "assembleDebug"
        )
    }
}

tasks.register("ciEmulatorAll") {
    group = CI_GRADLE
    doLast {
        listOf(29, 30, 31, 32, 33).forEach { apiLevelIt ->
            val name = listOf(
                "managedVirtualDevice",
                apiLevelIt.toString()
            ).joinToString(separator = "")
            gradlew(
                "${name}Check",
                "-Pandroid.testInstrumentationRunnerArguments.class=" +
                    "siarhei.luskanau.managed.virtual.device.ExampleInstrumentedTest",
                "-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect"
            )
        }
        gradlew("cleanManagedDevices")
    }
}

fun gradlew(
    vararg tasks: String,
    addToEnvironment: Map<String, String>? = null
) {
    exec {
        executable = File(
            project.rootDir,
            if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew"
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
