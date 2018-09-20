package com.winsonchiu.aria.itemsheet.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.winsonchiu.aria.itemsheet.ItemsMenuItem
import com.winsonchiu.aria.itemsheet.ItemsMenuView
import com.winsonchiu.aria.framework.util.DrawableUtils
import com.winsonchiu.aria.framework.util.initialize
import com.winsonchiu.aria.itemsheet.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.items_menu_icon_with_text_view.view.*

class ItemsMenuIconWithTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ItemsMenuView<ItemsMenuIconWithTextView.Model.Data> {

    init {
        initialize(R.layout.items_menu_icon_with_text_view)
        background = DrawableUtils.getDefaultRipple(context, false)
    }

    override fun bindData(data: Model.Data) {
        imageIcon.setImageResource(data.iconResource)
        text.setText(data.titleResource)
    }

    interface Model : ItemsMenuItem {

        override val clazz
            get() = ItemsMenuIconWithTextView::class

        @Parcelize
        data class Data(
                @DrawableRes
                val iconResource: Int,
                val titleResource: Int
        ) : Parcelable
    }
}