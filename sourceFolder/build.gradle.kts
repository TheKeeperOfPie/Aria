import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import org.jetbrains.kotlin.config.KotlinCompilerVersion
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt", project), "proguard-rules.pro")
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

dependencies {
    api(project (":queue"))
    api(project (":itemSheet"))

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    kaptDagger()

    val epoxyVersion = "3.0.0-rc1"
    kapt("com.airbnb.android:epoxy-processor:$epoxyVersion")
}
