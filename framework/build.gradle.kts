import com.android.build.gradle.ProguardFiles
import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

apply {
    plugin("submodule")
    plugin("com.jakewharton.butterknife")
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
