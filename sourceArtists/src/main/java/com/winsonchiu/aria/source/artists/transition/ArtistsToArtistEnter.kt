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
import com.winsonchiu.aria.framework.util.boundsAsRect
import com.winsonchiu.aria.source.artists.R
import com.winsonchiu.aria.source.artists.artists.ArtistItemView

class ArtistsToArtistEnter : GhostViewOverlay(
        overlayMode = OverlayMode.GHOST
) {

    companion object {
        private val PROP_VIEW_BOUNDS = "${ArtistsToArtistEnter::class.java.canonicalName}.viewBounds"
        private val PROP_IMAGE_BOUNDS = "${ArtistsToArtistEnter::class.java.canonicalName}.imageBounds"
        private val PROP_WINDOW_X = "${ArtistsToArtistEnter::class.java.canonicalName}.windowX"
        private val PROP_WINDOW_Y = "${ArtistsToArtistEnter::class.java.canonicalName}.windowY"
        private val IMAGE_WIDTH = "${ArtistsToArtistEnter::class.java.canonicalName}.imageWidth"
        private val IMAGE_HEIGHT = "${ArtistsToArtistEnter::class.java.canonicalName}.imageHeight"
        private val RADIUS = "${ArtistsToArtistEnter::class.java.canonicalName}.radius"
        private val BACKGROUND_COLOR = "${ArtistsToArtistEnter::class.java.canonicalName}.backgroundColor"
    }

    private val tempArray = IntArray(2)

    override fun onCaptureStart(
            view: View,
            values: MutableMap<String, Any?>
    ) {
        val image = view.findViewById<ImageView>(R.id.image)
        image.getLocationInWindow(tempArray)

        values[PROP_WINDOW_X] = tempArray[0]
        values[PROP_WINDOW_Y] = tempArray[1]
        values[PROP_VIEW_BOUNDS] = view.boundsAsRect()
        values[PROP_IMAGE_BOUNDS] = image.boundsAsRect()

        values[IMAGE_WIDTH] = image?.drawable?.intrinsicWidth ?: 0
        values[IMAGE_HEIGHT] = image?.drawable?.intrinsicHeight ?: 0

        values[RADIUS] = ((view as? ArtistItemView)?.outlineProvider as? RoundedOutlineProvider)?.radius
        values[BACKGROUND_COLOR] = (view as? ArtistItemView)?.paletteBackgroundColor
    }

    override fun onCaptureEnd(
            view: View,
            values: MutableMap<String, Any?>
    ) {
        val image = view.findViewById<ImageView>(R.id.imageArtist)

        image.getLocationInWindow(tempArray)

        values[PROP_WINDOW_X] = tempArray[0]
        values[PROP_WINDOW_Y] = tempArray[1]
        values[PROP_VIEW_BOUNDS] = image.boundsAsRect()
        values[PROP_IMAGE_BOUNDS] = image.boundsAsRect()

        values[IMAGE_WIDTH] = image?.drawable?.intrinsicWidth ?: 0
        values[IMAGE_HEIGHT] = image?.drawable?.intrinsicHeight ?: 0
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

        val image = endView?.findViewById<ImageView>(R.id.imageArtist) ?: return null
        val imageLayout = endView.findViewById<View>(R.id.imageArtistLayout) ?: return null

        val startViewBounds = startValues[PROP_VIEW_BOUNDS] as? Rect ?: return null
        val endViewBounds = endValues[PROP_VIEW_BOUNDS] as? Rect ?: return null

        val startImageBounds = startValues[PROP_IMAGE_BOUNDS] as? Rect ?: return null
        val endImageBounds = endValues[PROP_IMAGE_BOUNDS] as? Rect ?: return null

        val endImageWidth = endValues[IMAGE_WIDTH] as? Int
        val endImageHeight = endValues[IMAGE_HEIGHT] as? Int

        val startMatrix = ImageViewUtils.centerCropMatrix(
                imageWidth = endImageWidth,
                imageHeight = endImageHeight,
                viewWidth = startImageBounds.width(),
                viewHeight = startImageBounds.width()
        ) ?: MatrixUtils.IDENTITY_MATRIX

        val endMatrix = ImageViewUtils.centerCropMatrix(
                imageWidth = endImageWidth,
                imageHeight = endImageHeight,
                viewWidth = endImageBounds.width(),
                viewHeight = endImageBounds.height()
        ) ?: MatrixUtils.IDENTITY_MATRIX

        val drawable = image.drawable
        val drawableWidth = drawable?.intrinsicWidth ?: 0
        val drawableHeight = drawable?.intrinsicHeight ?: 0

        val matrixEvaluator = MatrixEvaluator()
        val animateMatrix = startImageBounds != endImageBounds
                && startMatrix != endMatrix
                && drawableWidth != 0
                && drawableHeight != 0

        if (animateMatrix) {
            ImageViewUtils.animateTransform(image, startMatrix)
        }

        val viewBounds = Rect()
        val imageBounds = Rect()
        val viewBoundsEvaluator = RectEvaluator(viewBounds)
        val imageBoundsEvaluator = RectEvaluator(imageBounds)

        val radius = startValues[RADIUS] as? Float ?: 0f
        val outlineProvider = RoundedOutlineProvider(0f)
        imageLayout.outlineProvider = outlineProvider

        (startValues[BACKGROUND_COLOR] as? Int)?.let {
            imageLayout.setBackgroundColor(it)
        }

        return ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                viewBoundsEvaluator.evaluate(it.animatedFraction, startViewBounds, endViewBounds)
                imageBoundsEvaluator.evaluate(it.animatedFraction, startImageBounds, endImageBounds)
                endView.setLeftTopRightBottom(viewBounds)
                image.setLeftTopRightBottom(imageBounds)
                imageLayout.setLeftTopRightBottom(0, 0, viewBounds.width(), viewBounds.height())

                outlineProvider.radius = it.animatedFraction.lerp(radius, 0f)

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