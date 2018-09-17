package com.winsonchiu.aria.folders.folder

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.TypedEpoxyController
import com.winsonchiu.aria.folders.folder.view.FileItemView
import com.winsonchiu.aria.folders.folder.view.FileItemViewModel_

class FolderEpoxyController(
        private val listener: FileItemView.Listener
) : TypedEpoxyController<List<EpoxyModel<*>>>() {

    override fun buildModels(data: List<EpoxyModel<*>>) {
        data.forEach {
            if (it is FileItemViewModel_ && it.listener() == null) {
                it.listener(listener)
            }

            it.addTo(this)
        }
    }
}