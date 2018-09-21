import com.android.build.gradle.ProguardFiles
import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

buildscript {
    repositories {
        mavenCentral()
        google()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        classpath("com.jakewharton:butterknife-gradle-plugin:9.0.0-SNAPSHOT")
    }
}

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

apply {
    plugin("com.jakewharton.butterknife")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(28)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(ProguardFiles.getDefaultProguardFile("proguard-android-optimize.txt", project), "proguard-rules.pro")
        }
    }

    dexOptions {
        preDexLibraries = true
    }

    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        (this as KotlinJvmOptions).jvmTarget = "1.8"
    }
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}

androidExtensions {
    configure(delegateClosureOf<AndroidExtensionsExtension> {
        isExperimental = true
    })
}

Dependencies(this) {
    api(Dependencies.Airbnb.epoxy)

    api(Dependencies.AndroidX.annotation)
    api(Dependencies.AndroidX.appcompat)
    api(Dependencies.AndroidX.browser)
    api(Dependencies.AndroidX.constraintLayout)
    api(Dependencies.AndroidX.coordinatorLayout)
    api(Dependencies.AndroidX.core)
    api(Dependencies.AndroidX.drawerLayout)
    api(Dependencies.AndroidX.fragment)
    api(Dependencies.AndroidX.lifecycle)
    api(Dependencies.AndroidX.palette)
    api(Dependencies.AndroidX.recyclerView)
    api(Dependencies.AndroidX.vectorDrawable)

    api(Dependencies.Google.dagger)
    api(Dependencies.Google.material)

    api(Dependencies.JakeWharton.butterKnife)

    api(Dependencies.Kotlin.stdlib)
    api(Dependencies.Kotlin.reflect)

    api(Dependencies.rxJava)

    api(Dependencies.Square.okHttp)
    api(Dependencies.Square.picasso)
    api(Dependencies.Square.retrofit2)

    api(Dependencies.Uber.autoDispose)

    "debugApi"(Dependencies.Square.LeakCanary.debug)
    "releaseApi"(Dependencies.Square.LeakCanary.release)
}
