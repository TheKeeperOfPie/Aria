package com.winsonchiu.aria.source.artists

import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.winsonchiu.aria.framework.fragment.arg
import com.winsonchiu.aria.source.artists.artist.ArtistFragment
import com.winsonchiu.aria.source.artists.artists.ArtistsFragment

object ArtistsToArtistTransition {

    fun image(artistId: ArtistId) = "${ArtistsToArtistTransition::class.java.canonicalName}.${artistId.value}.image"

    fun applyTo(before: ArtistsFragment, after: ArtistFragment) {
        before.exitTransition = Fade()
        before.reenterTransition = Fade()

        val artistId = after.arg(ArtistFragment.Args.artistId).value
        after.sharedElementEnterTransition = TransitionSet().apply {
            ChangeBounds()
                    .let(::addTransition)
            ChangeTransform()
                    .let(::addTransition)
            ChangeImageTransform()
                    .let(::addTransition)
        }
    }
}