import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import org.jetbrains.kotlin.cli.jvm.main
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt
import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules

plugins {
    id("com.android.application")
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
        applicationId = "com.winsonchiu.aria"
        minSdkVersion(28)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true

            signingConfig  = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android.txt", project), "proguard-rules.pro")
        }
    }

    dexOptions {
        preDexLibraries = true
    }

    sourceSets {
        getByName("main") {
            res.srcDirs(
                    "/src/main/res/home",
                    "/src/main/res/media",
                    "/src/main/res/res"
            )
        }
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
    implementation(Modules.nowPlaying)
    implementation(Modules.queue)
    implementation(Modules.sourceFolder)

    implementation(Dependencies.AndroidX.media)
    implementation(Dependencies.AndroidX.media2)

    implementation(Dependencies.Google.dagger)
}