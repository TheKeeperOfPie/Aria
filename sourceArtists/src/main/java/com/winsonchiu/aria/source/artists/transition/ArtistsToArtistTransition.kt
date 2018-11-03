package com.winsonchiu.aria.source.artists.transition

import androidx.transition.TransitionSet
import com.winsonchiu.aria.framework.fragment.arg
import com.winsonchiu.aria.framework.util.animation.transition.GhostViewOverlay
import com.winsonchiu.aria.framework.util.animation.transition.SlideAndFade
import com.winsonchiu.aria.source.artists.ArtistId
import com.winsonchiu.aria.source.artists.artist.ArtistFragment
import com.winsonchiu.aria.source.artists.artists.ArtistItemView
import com.winsonchiu.aria.source.artists.artists.ArtistsFragment

object ArtistsToArtistTransition {

    fun image(artistId: ArtistId) = "${ArtistsToArtistTransition::class.java.canonicalName}.${artistId.value}.image"
    fun header(artistId: ArtistId) = "${ArtistsToArtistTransition::class.java.canonicalName}.${artistId.value}.header"

    fun applyTo(
            view: ArtistItemView,
            before: ArtistsFragment,
            after: ArtistFragment
    ) {
        after.postponeEnterTransition()

        val artistId = ArtistId(after.arg(ArtistFragment.Args.artistId).value)
        before.exitTransition = TransitionSet().apply {
            SlideAndFade(
                    fadeFrom = 1f,
                    fadeTo = 0f,
                    overlayMode = GhostViewOverlay.OverlayMode.FRAMEWORK_VISIBILITY
            )
                    .forSupport()
                    .excludeTarget(
                            header(
                                    artistId
                            ), true)
                    .excludeTarget(
                            image(
                                    artistId
                            ), true)
                    .excludeTarget(view, true)
                    .let(::addTransition)
        }
                .setDuration(5000)
        before.reenterTransition = TransitionSet().apply {
            SlideAndFade(
                    fadeFrom = 0f,
                    fadeTo = 1f,
                    overlayMode = GhostViewOverlay.OverlayMode.FRAMEWORK_VISIBILITY
            )
                    .forSupport()
                    .excludeTarget(
                            header(
                                    artistId
                            ), true)
                    .excludeTarget(
                            image(
                                    artistId
                            ), true)
                    .excludeTarget(view, true)
                    .let(::addTransition)
        }
                .setDuration(5000)

        after.sharedElementEnterTransition = TransitionSet().apply {
            ArtistsToArtistEnter()
                    .forSupport()
                    .let(::addTransition)
        }
                .setDuration(5000)

        after.sharedElementReturnTransition = TransitionSet().apply {
            ArtistsToArtistReturn()
                    .forSupport()
                    .let(::addTransition)
        }
                .setDuration(5000)

//        before.exitTransition = Fade().setDuration(5000)

//        after.enterTransition = TransitionSet().apply {
//            SlideAndFade(
//                    fadeFrom = 0f,
//                    fadeTo = 1f
//            )
//                    .forSupport()
//                    .addTarget("artistFragmentRoot")
////                    .excludeTarget(ArtistsToArtistTransition.header(artistId), true)
////                    .excludeTarget(R.id.layoutAppBar, true)
////                    .excludeTarget(R.id.layoutCollapsingToolbar, true)
////                    .excludeTarget(R.id.imageArtistLayout, true)
////                    .excludeTarget(R.id.imageArtist, true)
//                    .let(::addTransition)
//        }
//                .setDuration(2500)
//        after.returnTransition = TransitionSet().apply {
//            SlideAndFade(
//                    fadeFrom = 1f,
//                    fadeTo = 0f
//            )
//                    .forSupport()
//                    .excludeTarget(ArtistsToArtistTransition.header(artistId), true)
//                    .excludeTarget(R.id.layoutAppBar, true)
//                    .excludeTarget(R.id.layoutCollapsingToolbar, true)
//                    .excludeTarget(R.id.imageArtistLayout, true)
//                    .excludeTarget(R.id.imageArtist, true)
//                    .let(::addTransition)
//        }
//                .setDuration(2500)
    }
}