import com.android.build.gradle.ProguardFiles
import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

apply {
    plugin("submodule")
    plugin("com.jakewharton.butterknife")
}

Dependencies(this) {
    api(Modules.framework)
}
