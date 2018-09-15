package com.winsonchiu.aria.nowplaying

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnNextLayout
import com.jakewharton.rxrelay2.BehaviorRelay
import com.uber.autodispose.android.ViewScopeProvider
import com.uber.autodispose.autoDisposable
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.util.FileUtils
import com.winsonchiu.aria.framework.util.activityComponent
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artwork.ArtworkCache
import com.winsonchiu.aria.music.artwork.ArtworkExtractor
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.now_playing_view_constraint_expanded_as_merge.view.*
import java.io.File
import javax.inject.Inject

class NowPlayingView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    @Inject
    lateinit var artworkExtractor: ArtworkExtractor

    @Inject
    lateinit var artworkCache: ArtworkCache

    private val requestRelay = BehaviorRelay.create<File>()

    init {
        initialize(R.layout.now_playing_view_constraint_expanded_as_merge)
        loadLayoutDescription(R.xml.now_playing_view_scene)

//        val sceneField = MotionLayout::class.java.getDeclaredField("mScene").apply {
//            isAccessible = true
//        }
//
//        val constraintSetMapField = MotionScene::class.java.getDeclaredField("mConstraintSetMap").apply {
//            isAccessible = true
//        }

//        val scene = sceneField.get(this)
//        val constraintSetMap = constraintSetMapField.get(scene) as SparseArray<ConstraintSet>

//        val endConstraintSet = ConstraintSet().apply {
//            clone(context, R.layout.now_playing_view_constraint_expanded)
//        }

//        val endConstraintSet = getEndConstraintSet()

//        setConstraintSet(endConstraintSet)

//        constraintSetMap.put(constraintSetMap.keyAt(0), endConstraintSet)
//        constraintSetMap.put(constraintSetMap.keyAt(1), endConstraintSet)

        context.activityComponent.inject(this)

        TypedValue().apply {
            context.theme.resolveAttribute(android.R.attr.windowBackground, this, true)
            setBackgroundColor(data)
        }

        doOnNextLayout {
            doOnNextLayout { progress = 0f }
            requestLayout()
        }
    }

    override fun setProgress(pos: Float) {
        super.setProgress(pos)

        textSongTitle.progress = 1f - pos
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

//        viewSquareBoundary.layoutParams = CustomParams(originalParams = viewSquareBoundary.layoutParams as LayoutParams)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        requestRelay
                .observeOn(Schedulers.computation())
                .switchMapMaybe { Maybe.fromCallable { artworkExtractor.getArtworkForFile(it, artworkCache)?.bitmap } }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(ViewScopeProvider.from(this))
                .subscribe { imageArtwork.setImageBitmap(it) }
    }

    fun bindData(data: Model) {
        val fileSortKey = FileUtils.getFileSortKey(data.file)
        val fileDisplayTitle = FileUtils.getFileDisplayTitle(fileSortKey?.substringBeforeLast("."))

        imageArtwork.setImageBitmap(null)

        textSongTitle.text = "Fate - Unlimited Blade Works - Brave Shine | Aimer - FateUBW"//fileDisplayTitle
        textSongDescription
                .text = "Fate - Unlimited Blade Works - Brave Shine | Aimer - FateUBW"//FileUtils.getFileDescription(context, data.metadata, true, true)

        requestRelay.accept(data.file)
    }

    data class Model(
            val file: File,
            val metadata: MetadataExtractor.Metadata?
    )

    class CustomParams(originalParams: ConstraintLayout.LayoutParams) : ConstraintLayout.LayoutParams(originalParams) {
        override fun validate() {
            super.validate()

            if (this.dimensionRatio == null) {
                return
            }

            val dimensionRatioSideField = ConstraintLayout.LayoutParams::class.java
                    .getDeclaredField("dimensionRatioSide").apply {
                        isAccessible = true
                    }
            val dimensionRatioSide = dimensionRatioSideField.get(this)

            val dimensionRatioValueField = ConstraintLayout.LayoutParams::class.java
                    .getDeclaredField("dimensionRatioValue").apply {
                        isAccessible = true
                    }

            val len = this.dimensionRatio.length
            var commaIndex = this.dimensionRatio.indexOf(",")
            if (commaIndex > 0 && commaIndex < len - 1) {
                val dimension = this.dimensionRatio.substring(0, commaIndex)
                if (dimension.equals("W", ignoreCase = true)) {
                    dimensionRatioSideField.set(this, 0)
                } else if (dimension.equals("H", ignoreCase = true)) {
                    dimensionRatioSideField.set(this, 1)
                }

                ++commaIndex
            } else {
                commaIndex = 0
            }

            val colonIndex = this.dimensionRatio.indexOf(':')
            val r: String
            if (colonIndex >= 0 && colonIndex < len - 1) {
                r = this.dimensionRatio.substring(commaIndex, colonIndex)
                val denominator = this.dimensionRatio.substring(colonIndex + 1)
                if (r.isNotEmpty() && denominator.isNotEmpty()) {
                    try {
                        val nominatorValue = java.lang.Float.parseFloat(r)
                        val denominatorValue = java.lang.Float.parseFloat(denominator)
                        if (nominatorValue > 0.0f && denominatorValue > 0.0f) {
                            if (dimensionRatioSide == 1) {
                                dimensionRatioValueField.set(this, Math.abs(denominatorValue / nominatorValue))
                            } else {
                                dimensionRatioValueField.set(this, Math.abs(nominatorValue / denominatorValue))
                            }
                        }
                    } catch (var16: NumberFormatException) {
                    }

                }
            } else {
                r = this.dimensionRatio.substring(commaIndex)
                if (r.isNotEmpty()) {
                    try {
                        dimensionRatioValueField.set(this, r.toFloat())
                    } catch (var15: NumberFormatException) {
                    }

                }
            }
        }
    }

    private fun getEndConstraintSet(): ConstraintSet {

        val constraintsField = ConstraintSet::class.java.getDeclaredField("mConstraints").apply {
            isAccessible = true
        }

        val constraintLayoutSetChildrenConstraintsMethod = ConstraintLayout::class.java
                .getDeclaredMethod("setChildrenConstraints").apply {
            isAccessible = true
        }

        val endConstraintLayout = LayoutInflater.from(context)
                .inflate(R.layout.now_playing_view_constraint_expanded, null, false) as ConstraintLayout

        val endConstraintSet = ConstraintSet()
        val endConstraintSetConstraints = constraintsField.get(endConstraintSet) as HashMap<Int, ConstraintSet.Constraint>

        constraintLayoutSetChildrenConstraintsMethod.invoke(endConstraintLayout)

        val constraintViewIdField = ConstraintSet.Constraint::class.java.getDeclaredField("mViewId").apply {
            isAccessible = true
        }

        val count = endConstraintLayout.childCount
        for (index in 0 until count) {
            val child = endConstraintLayout.getChildAt(index)
            val params = child.layoutParams as ConstraintLayout.LayoutParams

            val constraint = ConstraintSet.Constraint().apply {
                baselineToBaseline = params.baselineToBaseline
                bottomMargin = params.bottomMargin
                bottomToBottom = params.bottomToBottom
                bottomToTop = params.bottomToTop
                circleAngle = params.circleAngle
                circleConstraint = params.circleConstraint
                circleRadius = params.circleRadius
                constrainedHeight = params.constrainedHeight
                constrainedWidth = params.constrainedWidth
                dimensionRatio = params.dimensionRatio
                editorAbsoluteX = params.editorAbsoluteX
                editorAbsoluteY = params.editorAbsoluteY
                endMargin = params.marginEnd
                endToEnd = params.endToEnd
                endToStart = params.endToStart
                goneBottomMargin = params.goneBottomMargin
                goneEndMargin = params.goneEndMargin
                goneLeftMargin = params.goneLeftMargin
                goneRightMargin = params.goneRightMargin
                goneStartMargin = params.goneStartMargin
                goneTopMargin = params.goneTopMargin
                guideBegin = params.guideBegin
                guideEnd = params.guideEnd
                guidePercent = params.guidePercent
                heightDefault = params.matchConstraintDefaultHeight
                heightMax = params.matchConstraintMaxHeight
                heightMin = params.matchConstraintMinHeight
                heightPercent = params.matchConstraintPercentHeight
                horizontalBias = params.horizontalBias
                horizontalChainStyle = params.horizontalChainStyle
                horizontalWeight = params.horizontalWeight
                leftMargin = params.leftMargin
                leftToLeft = params.leftToLeft
                leftToRight = params.leftToRight
                mHeight = params.height
                mWidth = params.width
                orientation = params.orientation
                rightMargin = params.rightMargin
                rightToLeft = params.rightToLeft
                rightToRight = params.rightToRight
                startMargin = params.marginStart
                startToEnd = params.startToEnd
                startToStart = params.startToStart
                topMargin = params.topMargin
                topToBottom = params.topToBottom
                topToTop = params.topToTop
                verticalBias = params.verticalBias
                verticalChainStyle = params.verticalChainStyle
                verticalWeight = params.verticalWeight
                widthDefault = params.matchConstraintDefaultWidth
                widthMax = params.matchConstraintMaxWidth
                widthMin = params.matchConstraintMinWidth
                widthPercent = params.matchConstraintPercentWidth

                if (child is Barrier) {
                    child.validateParams()
                    mHelperType = 1
                    mBarrierDirection = child.type
                    mReferenceIds = child.referencedIds
                }
            }

            constraintViewIdField.set(constraint, child.id)

            endConstraintSetConstraints[child.id] = constraint
        }

        return endConstraintSet
    }
}