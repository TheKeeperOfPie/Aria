plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

gradlePlugin {
    plugins {
        register("aria-android") {
            id = "aria-android"
            implementationClass = "AriaAndroidPlugin"
        }
    }
}

repositories {
    jcenter()
}