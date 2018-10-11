import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

apply {
    plugin("submodule")
    plugin("com.jakewharton.butterknife")
}

Dependencies(this) {
    api(Modules.queue)
    api(Modules.itemSheet)

    api(Dependencies.Google.dagger)
    api(Dependencies.Airbnb.epoxy)

    api(Dependencies.JakeWharton.butterKnife)
}
