import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope

import org.gradle.kotlin.dsl.*

object Versions {
    val dagger = "2.16"
}

object Dependencies {
    val daggerAndroidProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"
    val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
}

fun DependencyHandlerScope.kaptDagger() {
    "kapt"(Dependencies.daggerAndroidProcessor)
    "kapt"(Dependencies.daggerCompiler)
}