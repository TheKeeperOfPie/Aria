import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules

apply {
    plugin("submodule")
}

Dependencies(this) {
    api(Modules.artwork)
    api(Modules.framework)
    api(Modules.media)
    api(Modules.queue)

    api(Dependencies.Google.dagger)
    api(Dependencies.Airbnb.epoxy)

    api(Dependencies.JakeWharton.butterKnife)

    api(Dependencies.AndroidX.media)
    api(Dependencies.AndroidX.media2)
}
