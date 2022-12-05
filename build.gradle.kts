// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application").version("7.3.1").apply(false)
    id("com.android.library").version("7.3.1").apply(false)
    kotlin("android").version("1.7.21").apply(false)
    kotlin("multiplatform").version("1.7.21").apply(false)
}

tasks.register("clean") {
    delete(rootProject.buildDir)
}