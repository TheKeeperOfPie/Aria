import com.android.build.gradle.ProguardFiles
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt
import com.winsonchiu.aria.Modules
import com.winsonchiu.aria.Dependencies

apply {
    plugin("submodule")
}

Dependencies(this) {
    api(Modules.artwork)
    api(Modules.dialog)
    api(Modules.framework)

    api(Dependencies.Google.dagger)
}
