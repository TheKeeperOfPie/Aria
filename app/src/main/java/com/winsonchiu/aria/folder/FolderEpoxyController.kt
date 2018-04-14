package com.winsonchiu.aria.folder

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.TypedEpoxyController

class FolderEpoxyController : TypedEpoxyController<FolderEpoxyController.Model>() {

    override fun buildModels(data: Model) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    data class Model(
            private val models: EpoxyModel<*>
    )
}