package com.winsonchiu.aria.framework.menu.itemsheet.view

import android.content.Context
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.jakewharton.rxrelay2.BehaviorRelay
import com.uber.autodispose.android.ViewScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import com.winsonchiu.aria.R
import com.winsonchiu.aria.folders.util.FileUtils
import com.winsonchiu.aria.framework.activity.DaggerComponentActivity
import com.winsonchiu.aria.framework.dagger.activity.ActivityComponent
import com.winsonchiu.aria.framework.menu.itemsheet.ItemsMenuItem
import com.winsonchiu.aria.framework.menu.itemsheet.ItemsMenuView
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.textOrGone
import com.winsonchiu.aria.music.MetadataExtractor
import com.winsonchiu.aria.music.artwork.ArtworkCache
import com.winsonchiu.aria.music.artwork.ArtworkExtractor
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.items_menu_title_subtitle_header_view.view.*
import java.io.File
import javax.inject.Inject

class ItemsMenuFileHeaderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ItemsMenuView<ItemsMenuFileHeaderView.Model.Data> {

    @Inject
    lateinit var artworkExtractor: ArtworkExtractor

    @Inject
    lateinit var artworkCache: ArtworkCache

    private val requestRelay = BehaviorRelay.create<File>()

    init {
        initialize(R.layout.items_menu_title_subtitle_header_view)
        (context.getSystemService(DaggerComponentActivity.ACTIVITY_COMPONENT) as ActivityComponent).inject(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        requestRelay
                .observeOn(Schedulers.computation())
                .switchMapMaybe { Maybe.fromCallable { artworkExtractor.getArtworkForFile(it, artworkCache)?.bitmap } }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(ViewScopeProvider.from(this))
                .subscribe { image.setImageBitmap(it) }
    }

    override fun bindData(data: Model.Data) {
        val fileSortKey = FileUtils.getFileSortKey(data.file)
        val fileDisplayTitle = FileUtils.getFileDisplayTitle(fileSortKey?.substringBeforeLast("."))

        image.setImageBitmap(null)

        textTitle.text = fileDisplayTitle
        textSubtitle.textOrGone = FileUtils.getFileDescription(context, data.metadata, true, true)

        requestRelay.accept(data.file)
    }

    interface Model : ItemsMenuItem {

        override val clazz
            get() = ItemsMenuFileHeaderView::class

        @Parcelize
        data class Data(
                @DrawableRes
                val file: File,
                val metadata: MetadataExtractor.Metadata?
        ) : Parcelable
    }
}