import org.gradle.internal.impldep.aQute.bnd.osgi.Constants.options
import org.gradle.kotlin.dsl.support.kotlinEap

buildscript {
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.0-alpha02")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.0-rc-57")
        classpath("com.jakewharton:butterknife-gradle-plugin:9.0.0-rc1")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

afterEvaluate {
    tasks.withType(JavaCompile::class) {
        options.compilerArgs.addAll(listOf("-Xmaxerrs", "5000"))
    }
}
