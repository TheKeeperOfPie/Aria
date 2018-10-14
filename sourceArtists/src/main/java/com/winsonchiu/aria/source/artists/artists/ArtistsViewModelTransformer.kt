package com.winsonchiu.aria.source.artists.artists

import android.content.Context
import com.airbnb.epoxy.EpoxyModel

object ArtistsViewModelTransformer {

    fun transform(context: Context, listener: ArtistItemView.Listener, model: ArtistsController.Model): List<EpoxyModel<*>> {
        return model.artists.map {
            ArtistItemViewModel_()
                    .id(it.id.value)
                    .artist(it)
                    .listener(listener)
                    .spanSizeOverride { _, _, _ -> 1}
        }
    }
}