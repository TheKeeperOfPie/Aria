import com.winsonchiu.aria.Dependencies
import com.winsonchiu.aria.Modules

apply {
    plugin("submodule")
}

Dependencies(this) {
    api(Modules.framework)
}
