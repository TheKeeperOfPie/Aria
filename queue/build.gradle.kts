import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules

apply {
    plugin("submodule")
}

Dependencies(this) {
    api(Modules.artwork)
    api(Modules.frameworkMedia)
    api(Modules.mediaTransport)

    api(Dependencies.Google.dagger)
    api(Dependencies.Airbnb.epoxy)

    api(Dependencies.JakeWharton.butterKnife)
    implementation(Dependencies.Square.moshi)
}
