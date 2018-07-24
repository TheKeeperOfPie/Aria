package com.winsonchiu.aria.framework.menu.itemsheet

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsonchiu.aria.R
import com.winsonchiu.aria.framework.fragment.FragmentArgument
import kotlinx.android.synthetic.main.items_menu_fragment.*

class ItemsMenuDialogFragment<DataType : ItemsMenuItem> : BottomSheetDialogFragment() {

    var items by FragmentArgument<ArrayList<DataType>>("items")

    companion object {

        fun <DataType : ItemsMenuItem> newInstance(items: List<DataType>) = ItemsMenuDialogFragment<DataType>().apply {
            this.items = (items as? ArrayList<DataType> ?: ArrayList(items))
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.items_menu_fragment, container, false)

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerItems.adapter = ItemsMenuAdapter(items, object : ItemsMenuAdapter.Listener<DataType> {

            @Suppress("UNCHECKED_CAST")
            override fun onClick(item: DataType) {
                (parentFragment as Listener<DataType>).onClick(item)
                dismiss()
            }
        })
    }

    interface Listener<DataType : ItemsMenuItem> {
        fun onClick(item: DataType)
    }
}