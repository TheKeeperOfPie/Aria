package com.winsonchiu.aria.folders.folder

import com.winsonchiu.aria.util.animation.transition.DoNothingAsOverlay
import com.winsonchiu.aria.util.animation.transition.GhostViewOverlay
import com.winsonchiu.aria.util.animation.transition.SlideAndFade

object FolderToFolderTransition {

    fun applyToFragment(folderFragment: FolderFragment) = folderFragment.apply {
        enterTransition = SlideAndFade(
                slideFractionFromY = 0.5f,
                slideFractionToY = 0f,
                fadeFrom = 0f,
                fadeTo = 1f,
                overlayMode = GhostViewOverlay.OverlayMode.GHOST
        ).forSupport()
        returnTransition = SlideAndFade(
                slideFractionFromY = 0f,
                slideFractionToY = 0.5f,
                fadeFrom = 1f,
                fadeTo = 0f,
                overlayMode = GhostViewOverlay.OverlayMode.FRAMEWORK_VISIBILITY
        ).forSupport()
        exitTransition = DoNothingAsOverlay.forSupport()
        reenterTransition = DoNothingAsOverlay.forSupport()
    }
}