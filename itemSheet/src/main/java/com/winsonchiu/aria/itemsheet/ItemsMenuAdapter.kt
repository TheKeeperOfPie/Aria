package com.winsonchiu.aria.itemsheet

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.full.primaryConstructor

class ItemsMenuAdapter<DataType : ItemsMenuItem>(
        val items: List<DataType>,
        val listener: Listener<DataType>
) : RecyclerView.Adapter<ItemsMenuAdapter.ViewHolder<DataType>>() {

    override fun getItemViewType(position: Int): Int {
        // Return unique position because there shouldn't be any recycling
        return position
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) = ViewHolder(
            parent,
            items[viewType],
            listener
    )

    override fun getItemCount() = items.size

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
            holder: ViewHolder<DataType>,
            position: Int
    ) {
        holder.bindData(items[position])
    }

    class ViewHolder<DataType : ItemsMenuItem>(
            parent: ViewGroup,
            item: ItemsMenuItem,
            listener: Listener<DataType>
    ) : RecyclerView.ViewHolder(
            item.clazz
                    .primaryConstructor!!
                    .call(parent.context, null, 0) as View
    ) {

        lateinit var item: ItemsMenuItem

        init {
            itemView.layoutParams = ViewGroup
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            @Suppress("UNCHECKED_CAST")
            itemView.setOnClickListener { listener.onClick(item as DataType) }
        }

        @Suppress("UNCHECKED_CAST")
        fun bindData(item: ItemsMenuItem) {
            this.item = item
            (itemView as ItemsMenuView<Any>).bindData(item.data)
        }
    }

    interface Listener<DataType : ItemsMenuItem> {
        fun onClick(item: DataType)
    }
}