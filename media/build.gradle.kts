import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules

apply {
    plugin("submodule")
}

Dependencies(this) {
    api(Modules.framework)
    api(Modules.mediaTransport)
    api(Modules.queue)

    api(Dependencies.Google.dagger)

    api(Dependencies.AndroidX.media)
    api(Dependencies.AndroidX.media2)
}
