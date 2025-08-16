package convention.android.emulator

data class EmulatorConfig(
    val avdName: String,
    val sdkId: String,
    val deviceType: String,
    val port: String
)

val ANDROID_EMULATORS = "system-images;android-34;google_apis".let { baseSystemImage ->
    listOf(
        EmulatorConfig(
            avdName = "test_android_emulator_x86_64",
            sdkId = "$baseSystemImage;x86_64",
            deviceType = "21",
            port = "5574"
        ),
        EmulatorConfig(
            avdName = "test_android_emulator_arm64-v8a",
            sdkId = "$baseSystemImage;arm64-v8a",
            deviceType = "21",
            port = "5578"
        )
    )
}
