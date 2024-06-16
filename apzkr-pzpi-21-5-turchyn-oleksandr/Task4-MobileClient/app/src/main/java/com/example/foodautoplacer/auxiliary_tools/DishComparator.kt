package com.example.foodautoplacer.auxiliary_tools

import com.example.foodautoplacer.dataclasses.DishCatalogItem

class DishComparator {
    companion object {
        val nameComparator = Comparator<DishCatalogItem> { object1, object2 ->
            val firstName = object1.name
            val secondName = object2.name
            when {
                firstName > secondName -> 1
                firstName < secondName -> -1
                else -> 0
            }
        }

        val rateAscendingComparator = Comparator<DishCatalogItem> { object1, object2 ->
            object1.rate - object2.rate
        }

        val rateDescendingComparator = Comparator<DishCatalogItem> { object1, object2 ->
            object2.rate - object1.rate
        }
    }
}