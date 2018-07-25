package com.winsonchiu.aria.nowplaying

import android.content.Context
import android.support.constraint.motion.MotionLayout
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import com.uber.autodispose.android.ViewScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.util.FileUtils
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.textOrGone
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artwork.ArtworkCache
import com.winsonchiu.aria.music.artwork.ArtworkExtractor
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.now_playing_view.view.*
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
        initialize(R.layout.now_playing_view)
        loadLayoutDescription(R.xml.now_playing_view_scene)

        TypedValue().apply {
            context.theme.resolveAttribute(android.R.attr.windowBackground, this, true)
            setBackgroundColor(data)
        }
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

        textSongTitle.text = fileDisplayTitle
        textSongDescription.textOrGone = FileUtils.getFileDescription(context, data.metadata, true, true)

        requestRelay.accept(data.file)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("NowPlayingView", "onInterceptTouchEvent called with $ev")
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("NowPlayingView", "onTouchEvent called with $event")
        return super.onTouchEvent(event)
    }

    data class Model(
            val file: File,
            val metadata: MetadataExtractor.Metadata?
    )
}