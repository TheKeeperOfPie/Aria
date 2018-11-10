plugins {
    `groovy`
    `kotlin-dsl`
    `java-gradle-plugin`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

gradlePlugin {
    plugins {
        register("submodule") {
            id = "submodule"
            implementationClass = "com.winsonchiu.aria.SubModulePlugin"
        }
    }
}

repositories {
    google()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    implementation("com.android.tools.build:gradle:3.4.0-alpha03")
}