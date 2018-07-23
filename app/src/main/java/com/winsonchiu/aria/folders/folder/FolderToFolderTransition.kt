package com.winsonchiu.aria.folders.folder

import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.util.animation.transition.DoNothingAsOverlay
import com.winsonchiu.aria.framework.util.animation.transition.GhostViewOverlay
import com.winsonchiu.aria.framework.util.animation.transition.SlideAndFade
import com.winsonchiu.aria.framework.util.animation.transition.TransitionSetSupport

object FolderToFolderTransition {

    fun applyToFragment(fragment: FolderFragment) = fragment.apply {
        enterTransition = TransitionSetSupport().apply {
            SlideAndFade(
                    slideFractionFromY = 0.75f,
                    slideFractionToY = 0f,
                    fadeFrom = 0f,
                    fadeTo = 1f,
                    overlayMode = GhostViewOverlay.OverlayMode.GHOST,
                    target = fragment.uniqueTransitionName + R.id.folderRecyclerView
            )
                    .forSupport()
                    .addToSet()

            SlideAndFade(
                    fadeFrom = 0f,
                    fadeTo = 1f,
                    overlayMode = GhostViewOverlay.OverlayMode.GHOST,
                    target = fragment.uniqueTransitionName + R.id.folderTitleText
            )
                    .forSupport()
                    .addToSet()
        }
        returnTransition = TransitionSetSupport().apply {
            SlideAndFade(
                    slideFractionFromY = 0f,
                    slideFractionToY = 0.75f,
                    fadeFrom = 1f,
                    fadeTo = 0f,
                    overlayMode = GhostViewOverlay.OverlayMode.FRAMEWORK_VISIBILITY,
                    target = fragment.uniqueTransitionName + R.id.folderRecyclerView
            )
                    .forSupport()
                    .addToSet()

            // TODO: Fix this by combining Animators
//            SlideAndFade(
//                    fadeFrom = 1f,
//                    fadeTo = 0f,
//                    overlayMode = GhostViewOverlay.OverlayMode.FRAMEWORK_VISIBILITY,
//                    target = fragment.uniqueTransitionName + R.id.folderTitleText,
//                    test = true
//            )
//                    .forSupport()
//                    .addToSet()
        }
        exitTransition = DoNothingAsOverlay.forSupport()
        reenterTransition = DoNothingAsOverlay.forSupport()
    }
}