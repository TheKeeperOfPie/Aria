import com.android.build.gradle.ProguardFiles
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

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3-M2")
    api("org.jetbrains.kotlin:kotlin-reflect:1.3-M2")
    api("androidx.constraintlayout:constraintlayout:2.0.0-alpha2")
    api("androidx.constraintlayout:constraintlayout-solver:2.0.0-alpha2")

    api("androidx.appcompat:appcompat:1.0.0-rc02")
    api("androidx.coordinatorlayout:coordinatorlayout:1.0.0-rc02")
    api("androidx.drawerlayout:drawerlayout:1.0.0-rc02")
    api("androidx.vectordrawable:vectordrawable-animated:1.0.0-rc02")
    api("androidx.browser:browser:1.0.0-rc02")
    api("androidx.fragment:fragment-ktx:1.0.0-rc02")
    api("androidx.palette:palette-ktx:1.0.0-rc02")
    api("androidx.recyclerview:recyclerview-selection:1.0.0-rc02")
    api("androidx.annotation:annotation:1.0.0-rc02")
    api("androidx.annotation:annotation:1.0.0-rc02")
    api("com.google.android.material:material:1.0.0-rc02")

    api("androidx.core:core-ktx:1.0.0-rc02")

    api("androidx.lifecycle:lifecycle-common-java8:2.0.0-rc01")
    api("androidx.lifecycle:lifecycle-extensions:2.0.0-rc01")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.0.0-rc01")
    api("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.0.0-rc01")

    api("com.google.dagger:dagger-android-support:${Versions.dagger}")
    api("com.google.dagger:dagger-android:${Versions.dagger}")
    api("com.google.dagger:dagger:${Versions.dagger}")
    api("com.squareup:javapoet:1.11.1")

    kaptDagger()

    val leakCanaryVersion = "1.6.1"
    "debugApi"("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
    "releaseApi"("com.squareup.leakcanary:leakcanary-android-no-op:$leakCanaryVersion")

    val butterKnifeVersion = "9.0.0-SNAPSHOT"
    api("com.jakewharton:butterknife:$butterKnifeVersion")
    kapt("com.jakewharton:butterknife-compiler:$butterKnifeVersion")

    api("com.squareup.okhttp3:okhttp:3.11.0")
    api("com.squareup.picasso:picasso:2.71828")
    api("com.squareup.retrofit2:retrofit:2.4.0")

    api("io.reactivex.rxjava2:rxjava:2.2.2")
    api("io.reactivex.rxjava2:rxkotlin:2.3.0")
    api("io.reactivex.rxjava2:rxandroid:2.1.0")
    api("com.jakewharton.rxrelay2:rxrelay:2.0.0")
    api("com.github.akarnokd:rxjava2-extensions:0.20.3")

    val autoDisposeVersion = "1.0.0-RC2"
    api("com.uber.autodispose:autodispose-android-archcomponents-ktx:$autoDisposeVersion")
    api("com.uber.autodispose:autodispose-android-archcomponents-test-ktx:$autoDisposeVersion")
    api("com.uber.autodispose:autodispose-android-archcomponents-test:$autoDisposeVersion")
    api("com.uber.autodispose:autodispose-android-archcomponents:$autoDisposeVersion")
    api("com.uber.autodispose:autodispose-android-ktx:$autoDisposeVersion")
    api("com.uber.autodispose:autodispose-android:$autoDisposeVersion")
    api("com.uber.autodispose:autodispose-ktx:$autoDisposeVersion")
    api("com.uber.autodispose:autodispose:$autoDisposeVersion")

    val epoxyVersion = "3.0.0-rc1"
    kapt("com.airbnb.android:epoxy-processor:$epoxyVersion")
    api("com.airbnb.android:epoxy:$epoxyVersion")
}
