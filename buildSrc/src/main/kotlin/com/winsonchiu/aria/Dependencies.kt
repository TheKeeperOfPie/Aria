package com.winsonchiu.aria

import com.winsonchiu.aria.Dependency.Kapt
import com.winsonchiu.aria.Dependency.Multiple
import com.winsonchiu.aria.Dependency.Single
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

// TODO: Make private
object Versions {

    val kotlin = "1.3-M2"

    object Airbnb {
        val epoxy = "3.0.0-rc1"
    }

    object AndroidX {

        val media = "1.0.0-rc02"
        val media2 = "1.0.0-alpha02"

        val core = "1.0.0-rc02"

        val appcompat = "1.0.0-rc02"
        val coordinatorLayout = "1.0.0-rc02"
        val drawerLayout = "1.0.0-rc02"
        val fragment = "1.0.0-rc02"
        val vectorDrawable = "1.0.0-rc02"
        val browser = "1.0.0-rc02"
        val palette = "1.0.0-rc02"
        val recyclerViewSelection = "1.0.0-rc02"
        val recyclerView = "1.0.0-rc02"
        val annotation = "1.0.0-rc02"
        val constraintLayout = "2.0.0-alpha2"

        object Lifecycle {
            val common = "2.0.0-rc01"
            val extensions = "2.0.0-rc01"
            val viewModel = "2.0.0-rc01"
            val reactiveStreams = "2.0.0-rc01"
        }
    }

    object Google {
        val dagger = "2.16"
        val material = "1.0.0-rc02"
    }

    object JakeWharton {
        val butterKnife = "9.0.0-SNAPSHOT"
        val rxRelay = "2.0.0"
    }

    object Square {
        val javapoet = "1.11.1"
        val leakCanary = "1.6.1"
        val okHttp = "3.11.0"
        val picasso = "2.71828"
        val retrofit2 = "2.4.0"
    }

    object RxJava {
        val base = "2.2.2"
        val kotlin = "2.3.0"
        val android = "2.1.0"
        val extensions = "0.20.3"
    }

    object Uber {

        object AutoDispose {
            val androidArchComponentsKtx = "1.0.0-RC2"
            val androidArchComponents = "1.0.0-RC2"
            val androidKtx = "1.0.0-RC2"
            val android = "1.0.0-RC2"
            val baseKtx = "1.0.0-RC2"
            val base = "1.0.0-RC2"
        }
    }
}

object Dependencies {

    object Airbnb {

        val epoxy = Multiple(
                Epoxy.processor,
                Epoxy.runtime
        )

        object Epoxy {
            val processor = kapt("com.airbnb.android:epoxy-processor", Versions.Airbnb.epoxy)
            val runtime = "com.airbnb.android:epoxy"(Versions.Airbnb.epoxy)
        }
    }

    object Kotlin {
        val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"(Versions.kotlin)
        val reflect = "org.jetbrains.kotlin:kotlin-reflect"(Versions.kotlin)
    }

    object AndroidX {

        val media = "androidx.media:media"(Versions.AndroidX.media)
        val media2 = "androidx.media2:media2"(Versions.AndroidX.media2)

        val core = "androidx.core:core-ktx"(Versions.AndroidX.core)

        val appcompat = "androidx.appcompat:appcompat"(Versions.AndroidX.appcompat)
        val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout"(Versions.AndroidX.coordinatorLayout)
        val drawerLayout = "androidx.drawerlayout:drawerlayout"(Versions.AndroidX.drawerLayout)
        val vectorDrawable = "androidx.vectordrawable:vectordrawable-animated"(Versions.AndroidX.vectorDrawable)
        val browser = "androidx.browser:browser"(Versions.AndroidX.browser)
        val fragment = "androidx.fragment:fragment-ktx"(Versions.AndroidX.fragment)
        val palette = "androidx.palette:palette-ktx"(Versions.AndroidX.palette)

        val recyclerViewSelection = "androidx.recyclerview:recyclerview-selection"(Versions.AndroidX.recyclerViewSelection)
        val recyclerViewBase = "androidx.recyclerview:recyclerview-selection"(Versions.AndroidX.recyclerView)
        val recyclerView = Multiple(
                recyclerViewSelection,
                recyclerViewBase
        )

        val annotation = "androidx.annotation:annotation"(Versions.AndroidX.annotation)

        val constraintLayoutBase = "androidx.constraintlayout:constraintlayout"(Versions.AndroidX.constraintLayout)
        val constraintLayoutSolver = "androidx.constraintlayout:constraintlayout-solver"(Versions.AndroidX.constraintLayout)
        val constraintLayout = Multiple(
                constraintLayoutBase,
                constraintLayoutSolver
        )

        val lifecycle = Multiple(
                Lifecycle.common,
                Lifecycle.extensions,
                Lifecycle.viewModel,
                Lifecycle.reactiveStreams
        )

        object Lifecycle {
            val common = "androidx.lifecycle:lifecycle-common-java8"(Versions.AndroidX.Lifecycle.common)
            val extensions = "androidx.lifecycle:lifecycle-extensions"(Versions.AndroidX.Lifecycle.extensions)
            val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx"(Versions.AndroidX.Lifecycle.viewModel)
            val reactiveStreams = "androidx.lifecycle:lifecycle-reactivestreams-ktx"(Versions.AndroidX.Lifecycle.reactiveStreams)
        }
    }

    object Google {
        val material = "com.google.android.material:material"(Versions.Google.material)

        val dagger = Multiple(
                Dagger.androidProcessor,
                Dagger.compiler,
                Dagger.androidSupport,
                Dagger.android,
                Dagger.runtime,
                kapt("com.squareup:javapoet", Versions.Square.javapoet),
                Square.javaPoet
        )

        object Dagger {

            val androidProcessor = kapt("com.google.dagger:dagger-android-processor", Versions.Google.dagger)
            val compiler = kapt("com.google.dagger:dagger-compiler", Versions.Google.dagger)

            val androidSupport = "com.google.dagger:dagger-android-support"(Versions.Google.dagger)
            val android = "com.google.dagger:dagger-android"(Versions.Google.dagger)
            val runtime = "com.google.dagger:dagger"(Versions.Google.dagger)

        }
    }

    object JakeWharton {

        val rxRelay = "com.jakewharton.rxrelay2:rxrelay"(Versions.JakeWharton.rxRelay)

        val butterKnife = Multiple(
                ButterKnife.compiler,
                ButterKnife.runtime
        )

        object ButterKnife {
            val runtime = "com.jakewharton:butterknife"(Versions.JakeWharton.butterKnife)
            val compiler = kapt("com.jakewharton:butterknife-compiler", Versions.JakeWharton.butterKnife)
        }
    }

    object Square {
        val javaPoet = "com.squareup:javapoet"(Versions.Square.javapoet)
        val okHttp = "com.squareup.okhttp3:okhttp"(Versions.Square.okHttp)
        val picasso = "com.squareup.picasso:picasso"(Versions.Square.picasso)
        val retrofit2 = "com.squareup.retrofit2:retrofit"(Versions.Square.retrofit2)

        object LeakCanary {
            val debug = "com.squareup.leakcanary:leakcanary-android"(Versions.Square.leakCanary)
            val release = "com.squareup.leakcanary:leakcanary-android-no-op"(Versions.Square.leakCanary)
        }
    }

    val rxJava = Multiple(
            RxJava.base,
            RxJava.kotlin,
            RxJava.android,
            RxJava.extensions,
            JakeWharton.rxRelay
    )

    object RxJava {
        val base = "io.reactivex.rxjava2:rxjava"(Versions.RxJava.base)
        val kotlin = "io.reactivex.rxjava2:rxkotlin"(Versions.RxJava.kotlin)
        val android = "io.reactivex.rxjava2:rxandroid"(Versions.RxJava.android)
        val extensions = "com.github.akarnokd:rxjava2-extensions"(Versions.RxJava.extensions)
    }

    object Uber {
        val autoDispose = Multiple(
                AutoDispose.androidArchComponentsKtx,
                AutoDispose.androidArchComponents,
                AutoDispose.androidKtx,
                AutoDispose.android,
                AutoDispose.baseKtx,
                AutoDispose.base
        )

        object AutoDispose {
            val androidArchComponentsKtx = "com.uber.autodispose:autodispose-android-archcomponents-ktx"(Versions.Uber.AutoDispose.androidArchComponentsKtx)
            val androidArchComponents = "com.uber.autodispose:autodispose-android-archcomponents"(Versions.Uber.AutoDispose.androidArchComponents)
            val androidKtx = "com.uber.autodispose:autodispose-android-ktx"(Versions.Uber.AutoDispose.androidKtx)
            val android = "com.uber.autodispose:autodispose-android"(Versions.Uber.AutoDispose.android)
            val baseKtx = "com.uber.autodispose:autodispose-ktx"(Versions.Uber.AutoDispose.baseKtx)
            val base = "com.uber.autodispose:autodispose"(Versions.Uber.AutoDispose.base)
        }
    }

    private fun kapt(
            value: String,
            version: String
    ) = Kapt.constructDoNotCall("$value:$version")

    private operator fun String.invoke(version: String) = Single.constructDoNotCall("$this:$version")

    operator fun invoke(
            project: Project,
            block: WrapperDependencyHandler.() -> Unit
    ) =
            WrapperDependencyHandler(project.dependencies).block()
}

object Modules {
    val artwork = Module("artwork")
    val framework = Module("framework")
    val itemSheet = Module("itemSheet")
    val queue = Module("queue")
    val sourceFolder = Module("sourceFolder")
}

sealed class Dependency {

    abstract operator fun invoke(
            dependencyHandler: DependencyHandler,
            configuration: String
    )

    class Single private constructor(private val value: String) : Dependency() {

        companion object {
            fun constructDoNotCall(value: String) = Single(value)
        }

        override fun invoke(
                dependencyHandler: DependencyHandler,
                configuration: String
        ) {
            dependencyHandler.add(configuration, value)
        }
    }

    class Kapt private constructor(private val value: String) : Dependency() {

        companion object {
            fun constructDoNotCall(value: String) = Kapt(value)
        }

        override fun invoke(
                dependencyHandler: DependencyHandler,
                configuration: String
        ) {
            dependencyHandler.add("kapt", value)
        }
    }

    class Multiple(private vararg val dependencies: Dependency) : Dependency() {
        override fun invoke(
                dependencyHandler: DependencyHandler,
                configuration: String
        ) {
            dependencies.forEach { it.invoke(dependencyHandler, configuration) }
        }
    }
}

data class Module(val value: String) {

    operator fun invoke(
            dependencyHandler: DependencyHandler,
            configuration: String
    ) = dependencyHandler.add(configuration, dependencyHandler.project(":$value"))
}

fun DependencyHandlerScope.projectArtwork() {

}

class WrapperDependencyHandler(dependencyHandler: DependencyHandler) : DependencyHandler by dependencyHandler {

    fun api(dependency: Dependency) {
        dependency(this, "api")
    }

    fun implementation(dependency: Dependency) {
        dependency(this, "implementation")
    }

    operator fun String.invoke(dependency: Dependency) {
        dependency(this@WrapperDependencyHandler, this)
    }

    fun api(module: Module) {
        module(this, "api")
    }

    fun implementation(module: Module) {
        module(this, "implementation")
    }

    operator fun String.invoke(module: Module) {
        module(this@WrapperDependencyHandler, this)
    }
}