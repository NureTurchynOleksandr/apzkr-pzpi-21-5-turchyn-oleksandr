package com.example.foodautoplacer.activities.dishordering

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodautoplacer.BuildConfig
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.basic.ActivityWithTopMenu
import com.example.foodautoplacer.activities.catalog.DishActivity
import com.example.foodautoplacer.dataclasses.CartDish
import com.example.foodautoplacer.dataclasses.CartDishIngredient
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_dish.*
import kotlinx.android.synthetic.main.view_cart_row.view.*
import kotlinx.android.synthetic.main.view_send_report.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder

class CartActivity : ActivityWithTopMenu() {
    private var establishmentId = 2
    private val catalogDishContainer: ArrayList<CartDish> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        requestQueue = Volley.newRequestQueue(this)

        getUserCartContent()
    }

    private fun getUserCartContent() {
        val url: String = BuildConfig.API_URL + BuildConfig.CART +
                "?catering_establishment=$establishmentId"

        val req: JsonArrayRequest =
            object : JsonArrayRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    saveUserCartContent(response)
                },
                Response.ErrorListener { error ->
                    Log.d("", "")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
                    val accessToken = "JWT " + prefs.getString("access_token", "").toString()
                    params["Authorization"] = accessToken
                    return params
                }
            }
        requestQueue.add(req)
    }

    private fun saveUserCartContent(response: JSONArray) {
        val cartTable: TableLayout = tableLayout
        val inflater: LayoutInflater = LayoutInflater.from(this)

        var totalSum = 0.0

        for (i in 0 until response.length()) {
            val currentDishObject = response[i] as JSONObject
            val currentPrice = currentDishObject["price"] as Double
            val view = inflater.inflate(
                R.layout.view_cart_row,
                cartTable, false)
            view.cart_item_name.text = currentDishObject["name"] as String
            view.cart_item_price.text = getStringPriceRepresentation(currentPrice)

            totalSum += currentPrice

            val currentDishObjectIngredients = currentDishObject["ingredients"] as JSONArray

            for (j in 0 until currentDishObjectIngredients.length()) {
                val currentIngredient = currentDishObjectIngredients[j] as JSONArray

                val newIngredient = TextView(applicationContext)
                newIngredient.text = currentIngredient[0] as String
                view.cart_item_ingredients.addView(newIngredient)

                val newIngredientWeight = TextView(applicationContext)
                val currentIngredientIsLiquid = currentIngredient[2] as Boolean
                val currentIngredientMeasure = if (currentIngredientIsLiquid)
                    resources.getString(R.string.dish_volume_measure)
                else resources.getString(R.string.dish_weight_measure)
                newIngredientWeight.text =
                    "${getIngredientMeasure(currentIngredient[1] as Double, 
                        currentIngredientIsLiquid)} $currentIngredientMeasure"
                view.cart_item_ingredients_weight.addView(newIngredientWeight)
            }

            view.cart_item_delete.setOnClickListener {
                val totalSumString = total_cart_sum_value.text.toString()
                val totalSumStringLength = totalSumString.length
                val currentTotalSum = if (currentLocale == "en")
                    totalSumString.substring(1, totalSumStringLength - 1).toDouble()
                else totalSumString.substring(0, totalSumStringLength - 2).toDouble()
                total_cart_sum_value.text =
                    getStringPriceRepresentation(
                        currentTotalSum - currentPrice.toFloat()
                    )
                cartTable.removeView(view)
            }

            view.tag = currentDishObject["id"]

            cartTable.addView(view)
        }
        total_cart_sum_value.text = getStringPriceRepresentation(totalSum)
    }

    fun emptyCart(view: View) {
        tableLayout.removeViews(2, tableLayout.childCount - 2)
        total_cart_sum_value.text = getStringPriceRepresentation(0.0)
    }

    fun sendCartPutRequest(view: View) {
        val actualCartItems = arrayListOf<Int>()
        for (child in 2 until tableLayout.childCount) {
            val currentChild = tableLayout.getChildAt(child)
            actualCartItems.add(currentChild.tag as Int)
        }

        val params = StringBuilder()
        for (i in 0 until actualCartItems.size) {
            params.append("0=${actualCartItems[i]}&")
        }

        val url = StringBuilder()
        url.append(BuildConfig.API_URL + BuildConfig.CART +
                "?catering_establishment=$establishmentId&" + params)
        url.deleteCharAt(url.length - 1)

        val req: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.PUT, url.toString(), null,
                Response.Listener { response ->
                    Log.d("", "")
                },
                Response.ErrorListener { error ->
                    Log.d("", "")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
                    val accessToken = "JWT " + prefs.getString("access_token", "").toString()
                    params["Authorization"] = accessToken
                    return params
                }
            }
        requestQueue.add(req)
    }

    fun makeOrder(view: View) {
        val url: String = BuildConfig.API_URL + BuildConfig.ORDER

        val requestBodyMap = HashMap<String,String>()
        requestBodyMap["catering_establishment"] = establishmentId.toString()
        val requestBody = JSONObject(requestBodyMap as Map<*, *>)

        val req: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                Response.Listener { response ->
                    val orderIntent = Intent(this,
                        OrderActivity::class.java
                    )
                    startActivity(orderIntent)
                },
                Response.ErrorListener { error ->
                    Log.d("", "")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
                    val accessToken = "JWT " + prefs.getString("access_token", "").toString()
                    params["Authorization"] = accessToken
                    return params
                }
            }
        requestQueue.add(req)
    }
}