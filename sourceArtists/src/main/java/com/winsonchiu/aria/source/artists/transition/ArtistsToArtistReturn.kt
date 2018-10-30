package com.winsonchiu.aria.source.artists.transition

import android.animation.Animator
import android.animation.RectEvaluator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.animation.MatrixEvaluator
import com.winsonchiu.aria.framework.util.ImageViewUtils
import com.winsonchiu.aria.framework.util.MatrixUtils
import com.winsonchiu.aria.framework.util.RoundedOutlineProvider
import com.winsonchiu.aria.framework.util.animation.lerp
import com.winsonchiu.aria.framework.util.animation.transition.GhostViewOverlay
import com.winsonchiu.aria.framework.util.animation.transition.setLeftTopRightBottom
import com.winsonchiu.aria.source.artists.R
import com.winsonchiu.aria.source.artists.artists.ArtistItemView

class ArtistsToArtistReturn : GhostViewOverlay(
        OverlayMode.GHOST
) {

    companion object {
        private val PROP_BOUNDS = "${ArtistsToArtistReturn::class.java.canonicalName}.bounds"
        private val PROP_IMAGE_BOUNDS = "${ArtistsToArtistReturn::class.java.canonicalName}.imageBounds"
        private val IMAGE_WIDTH = "${ArtistsToArtistReturn::class.java.canonicalName}.imageWidth"
        private val IMAGE_HEIGHT = "${ArtistsToArtistReturn::class.java.canonicalName}.imageHeight"
        private val RADIUS = "${ArtistsToArtistReturn::class.java.canonicalName}.radius"
    }

    override fun onCaptureStart(
            view: View,
            values: MutableMap<String, Any?>
    ) {
        val image = view.findViewById<ImageView>(R.id.imageArtist)

        values[PROP_BOUNDS] = Rect(
                view.left,
                view.top,
                view.right,
                view.bottom
        )

        values[IMAGE_WIDTH] = image?.drawable?.intrinsicWidth ?: 0
        values[IMAGE_HEIGHT] = image?.drawable?.intrinsicHeight ?: 0
    }

    override fun onCaptureEnd(
            view: View,
            values: MutableMap<String, Any?>
    ) {
        val image = view.findViewById<ImageView>(R.id.image)

        values[PROP_BOUNDS] = Rect(
                view.left,
                view.top,
                view.right,
                view.bottom
        )

        values[PROP_IMAGE_BOUNDS] = Rect(
                image.left,
                image.top,
                image.right,
                image.bottom
        )

        values[IMAGE_WIDTH] = image?.drawable?.intrinsicWidth ?: 0
        values[IMAGE_HEIGHT] = image?.drawable?.intrinsicHeight ?: 0

        values[RADIUS] = ((view as? ArtistItemView)?.outlineProvider as? RoundedOutlineProvider)?.radius
    }

    override fun onCreateAnimator(
            sceneRoot: ViewGroup,
            startView: View?,
            endView: View?,
            startValues: MutableMap<String, Any?>?,
            endValues: MutableMap<String, Any?>?
    ): Animator? {
        startValues ?: return null
        endValues ?: return null

        val view = endView ?: return null
        val image = view.findViewById<ImageView>(R.id.image)

        val startBounds = startValues[PROP_BOUNDS] as? Rect ?: return null
        val endBounds = endValues[PROP_BOUNDS] as? Rect ?: return null
        val endImageBounds = endValues[PROP_IMAGE_BOUNDS] as? Rect ?: return null

        val endImageWidth = endValues[IMAGE_WIDTH] as? Int
        val endImageHeight = endValues[IMAGE_HEIGHT] as? Int

        val startMatrix = ImageViewUtils.centerCropMatrix(
                imageWidth = endImageWidth,
                imageHeight = endImageHeight,
                viewWidth = startBounds.width(),
                viewHeight = startBounds.height()
        ) ?: MatrixUtils.IDENTITY_MATRIX

        val endMatrix = ImageViewUtils.centerCropMatrix(
                imageWidth = endImageWidth,
                imageHeight = endImageHeight,
                viewWidth = endBounds.width(),
                viewHeight = endBounds.width()
        ) ?: MatrixUtils.IDENTITY_MATRIX

        val drawable = image.drawable
        val drawableWidth = drawable?.intrinsicWidth ?: 0
        val drawableHeight = drawable?.intrinsicHeight ?: 0

        val matrixEvaluator = MatrixEvaluator()
        val animateMatrix = startBounds != endBounds
                && startMatrix != endMatrix
                && drawableWidth != 0
                && drawableHeight != 0

        if (animateMatrix) {
            ImageViewUtils.animateTransform(image, startMatrix)
        }

        val bounds = Rect()
        val imageBounds = Rect()
        val boundsEvaluator = RectEvaluator(bounds)
        val imageBoundsEvaluator = RectEvaluator(imageBounds)

        val radius = endValues[RADIUS] as? Float ?: 0f
        val outlineProvider = view.outlineProvider as? RoundedOutlineProvider

        return ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                boundsEvaluator.evaluate(it.animatedFraction, startBounds, endBounds)
                imageBoundsEvaluator.evaluate(it.animatedFraction, startBounds, endImageBounds)
                view.setLeftTopRightBottom(bounds)
                image.setLeftTopRightBottom(imageBounds)

                outlineProvider?.radius = it.animatedFraction.lerp(0f, radius)

                if (animateMatrix) {
                    ImageViewUtils
                            .animateTransform(
                                    image,
                                    matrixEvaluator.evaluate(it.animatedFraction, startMatrix, endMatrix)
                            )
                }
            }
        }
    }
}