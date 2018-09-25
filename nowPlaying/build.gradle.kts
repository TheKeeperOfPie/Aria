import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules

apply {
    plugin("submodule")
    plugin("com.jakewharton.butterknife")
}

Dependencies(this) {
    api(Modules.artwork)
    api(Modules.framework)
    api(Modules.queue)

    api(Dependencies.Google.dagger)
    api(Dependencies.Airbnb.epoxy)

    api(Dependencies.JakeWharton.butterKnife)
}
