rootProject.name = "android-managed-virtual-device"
include(
    ":app"
)

pluginManagement {
    includeBuild("convention-android-emulator")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
