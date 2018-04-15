package com.winsonchiu.aria.folder

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.TypedEpoxyController

class FolderEpoxyController : TypedEpoxyController<FolderEpoxyController.Model>() {

    override fun buildModels(data: Model) {
        data.models.forEach { it.addTo(this) }
    }

    data class Model(
            val models: List<EpoxyModel<*>>
    )
}