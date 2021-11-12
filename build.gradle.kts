buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean").configure {
    delete("build")
}