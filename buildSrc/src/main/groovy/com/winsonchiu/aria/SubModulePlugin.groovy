package com.winsonchiu.aria

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

class SubModulePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("kotlin-android")
        project.plugins.apply("kotlin-android-extensions")
        project.plugins.apply("kotlin-kapt")

        project.plugins.withId("com.android.library") {

            project.android {

                compileSdkVersion 28

                defaultConfig {
                    minSdkVersion 28
                    targetSdkVersion 28
                    versionCode = 1
                    versionName = "0.1"
                    vectorDrawables.useSupportLibrary true

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        minifyEnabled false
                        proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
                    }
                }

                dexOptions {
                    preDexLibraries = true
                }

                compileOptions {
                    sourceCompatibility JavaVersion.VERSION_1_8
                    targetCompatibility JavaVersion.VERSION_1_8
                }

                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }

            project.kapt {
                correctErrorTypes true
                useBuildCache true
            }

            project.androidExtensions {
                experimental true
            }
        }
    }
}