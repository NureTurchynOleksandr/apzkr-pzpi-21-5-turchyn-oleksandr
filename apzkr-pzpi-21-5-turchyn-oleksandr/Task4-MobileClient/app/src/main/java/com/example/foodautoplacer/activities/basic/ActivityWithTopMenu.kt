package com.example.foodautoplacer.activities.basic

import android.os.Bundle
import android.view.MenuItem
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.authentification.AuthorizationActivity
import com.example.foodautoplacer.activities.catalog.DishCatalog
import com.example.foodautoplacer.activities.dishordering.CartActivity
import com.example.foodautoplacer.activities.dishordering.OrderActivity
import com.example.foodautoplacer.activities.search.CateringEstablishmentSearchActivity
import com.example.foodautoplacer.activities.search.DishSearchActivity
import kotlin.math.roundToInt

open class ActivityWithTopMenu : GeneralApplicationActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.log_out_menu_item -> {
                eraseAccessToken()
                startNewActivity(AuthorizationActivity::class.java)
                return true
            }
            R.id.change_language_menu_item -> {
                changeUserInterfaceLanguage()
                return true
            }
            R.id.catalog_menu_item -> {
                startNewActivity(DishCatalog::class.java)
                return true
            }
            R.id.dish_search_menu_item -> {
                startNewActivity(DishSearchActivity::class.java)
                return true
            }
            R.id.catering_establishment_search_menu_item -> {
                startNewActivity(CateringEstablishmentSearchActivity::class.java)
                return true
            }
            R.id.cart_menu_item -> {
                startNewActivity(CartActivity::class.java)
                return true
            }
            R.id.order_menu_item -> {
                startNewActivity(OrderActivity::class.java)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMenu = R.menu.activity_with_top_menu_menu
    }

    fun getRoundedDoubleValue(value: Double): Double {
        val doubleRepresentationWithAppropriatePrecision: Double =
            value * 100
        val intRepresentationWithAppropriatePrecision =
            doubleRepresentationWithAppropriatePrecision.roundToInt()
        return intRepresentationWithAppropriatePrecision.toDouble() / 100
    }

    fun getStringPriceRepresentation(current_price: Double): String {
        var priceToDisplay = if (currentLocale == "en") "$" else ""
        priceToDisplay += if (currentLocale == "en")
            (getRoundedDoubleValue(current_price / 27.8)).toString()
        else getRoundedDoubleValue(current_price).toString()
        priceToDisplay += if (currentLocale == "en") "" else " ₴"
        return priceToDisplay
    }

    fun getIngredientMeasure(
        ingredientWeightOrVolume: Double,
        isLiquid: Boolean
    ): Double {
        return if (currentLocale.startsWith("uk"))
            getRoundedDoubleValue(ingredientWeightOrVolume)
        else getRoundedDoubleValue(ingredientWeightOrVolume /
                if (isLiquid) 0.028 else 453.59)
    }
}