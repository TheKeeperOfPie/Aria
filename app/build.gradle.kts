import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import org.jetbrains.kotlin.cli.jvm.main
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
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
                    "/src/main/res/nowplaying",
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

dependencies {
    implementation(project(":queue"))
    implementation(project(":sourceFolder"))

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("androidx.media:media:1.0.0-rc02")
    implementation("androidx.media2:media2:1.0.0-alpha02")

    implementation("androidx.paging:paging-runtime:2.0.0-rc01")
    implementation("androidx.paging:paging-rxjava2:2.0.0-rc01")

    kaptDagger()
}