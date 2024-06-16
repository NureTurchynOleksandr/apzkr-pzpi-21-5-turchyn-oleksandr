package com.example.foodautoplacer.auxiliary_tools

import android.content.Context
import android.widget.ArrayAdapter
import java.util.ArrayList

class SearchAdapter<T>(context: Context, layout: Int, var resource: ArrayList<T>) :
    ArrayAdapter<T>(context, layout, resource) {

    val ingredientArray = resource as ArrayList<Pair<String, Int>>

    override fun getItemId(position: Int): Long {
        return ingredientArray[position].second.toLong()
    }

    override fun getItem(position: Int): T? {
        return ingredientArray[position].first as T?
    }
}