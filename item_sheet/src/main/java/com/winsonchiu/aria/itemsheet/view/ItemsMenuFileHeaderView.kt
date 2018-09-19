package com.winsonchiu.aria.itemsheet.view

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso
import com.winsonchiu.aria.artwork.ArtworkTransformation
import com.winsonchiu.aria.framework.dagger.activity.DaggerConstants
import com.winsonchiu.aria.framework.util.dpToPx
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.framework.util.textOrGone
import com.winsonchiu.aria.itemsheet.ItemsMenuItem
import com.winsonchiu.aria.itemsheet.ItemsMenuView
import com.winsonchiu.aria.itemsheet.ItemsViewInjector
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.items_menu_title_subtitle_header_view.view.*

class ItemsMenuFileHeaderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ItemsMenuView<ItemsMenuFileHeaderView.Model.Data> {

    private val artworkTransformation = ArtworkTransformation(40.dpToPx(context))

    init {
        initialize(com.winsonchiu.aria.itemsheet.R.layout.items_menu_title_subtitle_header_view)
        (context.getSystemService(DaggerConstants.ACTIVITY_COMPONENT) as ItemsViewInjector).inject(this)
    }

    override fun bindData(data: Model.Data) {
        Picasso.get()
                .load(data.image)
                .transform(artworkTransformation)
                .into(image)

        textTitle.text = data.title
        textSubtitle.textOrGone = data.description
    }

    interface Model : ItemsMenuItem {

        override val clazz
            get() = ItemsMenuFileHeaderView::class

        @Parcelize
        data class Data(
                val title: CharSequence?,
                val description: CharSequence?,
                val image: Uri?
        ) : Parcelable
    }
}