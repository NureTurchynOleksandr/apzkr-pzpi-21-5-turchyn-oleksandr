package com.example.foodautoplacer.activities.catalog

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodautoplacer.BuildConfig
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.basic.ActivityWithTopMenu
import com.example.foodautoplacer.dataclasses.DishCatalogItem
import com.example.foodautoplacer.dataclasses.Ingredient
import com.example.foodautoplacer.fragments.AlertFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_dish.*
import kotlinx.android.synthetic.main.view_ingredient_row.view.*
import kotlinx.android.synthetic.main.view_send_report.view.*
import kotlinx.android.synthetic.main.view_single_report.*
import kotlinx.android.synthetic.main.view_single_report.view.*
import kotlinx.android.synthetic.main.view_sum_ingredients.view.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class DishActivity : ActivityWithTopMenu() {
    private var dishId by Delegates.notNull<Int>()
    private var currentDish by Delegates.notNull<DishCatalogItem>()
    private var price: Double = 0.0
    private val ingredients: ArrayList<Ingredient> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish)

        requestQueue = Volley.newRequestQueue(this)
        dishId = intent.getIntExtra("dishId", 0)
        getDishInfo()
        getDishIngredientsData()
        getAllDishReports()

        setExpandingListener(description_arrow_button, expandable_constraint_description)
        setExpandingListener(ingredients_arrow_button, expandable_constraint_ingredients)
        setExpandingListener(reports_arrow_button, expandable_constraint_reports)
    }

    private fun getDishInfo() {
        val url: String = BuildConfig.API_URL + BuildConfig.DISH + "$dishId/"

        val req: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    saveDishInfo(response)
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

    private fun saveDishInfo(response: JSONObject) {
        currentDish = DishCatalogItem(
            response["id"] as Int,
            response["name"] as String,
            response["image"] as String,
            response["description"] as String,
            response["type"] as String,
            response["popularity"] as String,
            response["rate"] as Int
        )
        fillDishInfo()
    }

    private fun fillDishInfo() {
        dishName.text = currentDish.name
        dishRatingBar.rating = currentDish.rate.toFloat()
        dishType.text = currentDish.type
        dishPopularity.text = currentDish.popularity
        if (currentDish.popularity == "") {
            dishPopularity.visibility = View.INVISIBLE
        }
        Picasso.get().load(currentDish.image).into(dishImage)
        dishDescription.text = currentDish.description
    }

    private fun getDishIngredientsData() {
        val url: String = BuildConfig.API_URL +
                BuildConfig.DISH_INGREDIENTS_PRECISE_DATA + "$dishId/"

        val req: JsonArrayRequest =
            object : JsonArrayRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    fillIngredientList(response)
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

    private fun fillIngredientList(response: JSONArray) {
        val ingredientListLayout: LinearLayout = list_ingredients_view
        val inflater: LayoutInflater = LayoutInflater.from(this)

        price = getIngredientPriceSum(response)
        val sumPriceView = inflater.inflate(
            R.layout.view_sum_ingredients,
            ingredientListLayout, false)

        sumPriceView.add_dish_to_cart_button.setOnClickListener{
            sendDishToCart()
        }

        for (i in 0 until response.length()) {
            val ingredientView = inflater.inflate(
                R.layout.view_ingredient_row,
                ingredientListLayout, false)
            val currentIngredient = response[i] as JSONObject
            ingredientView.ingredient_name.text = currentIngredient.getString("name")
            val ingredientWeightOrVolume = currentIngredient.getDouble("weight_or_volume")
            val measureValue = getIngredientMeasure(
                ingredientWeightOrVolume,
                currentIngredient.getBoolean("is_liquid")
            )
            ingredientView.ingredient_value.text = measureValue.toString()
            ingredientView.ingredient_measure.text =
                if (currentIngredient.getBoolean("is_liquid"))
                    resources.getString(R.string.dish_volume_measure)
                else resources.getString(R.string.dish_weight_measure)
            ingredientView.increase_ingredient_value_button.setOnClickListener {
                val ingredientValue = (ingredientView.ingredient_value.text as String).toDouble()
                ingredientView.ingredient_value.text = (ingredientValue + 1).toString()
                price += currentIngredient.getDouble("price")
                sumPriceView.sum_dish_price_value.text = getStringPriceRepresentation(price)
                if (ingredientValue == 0.0) {
                    ingredientView.decrease_ingredient_value_button.isEnabled = true
                }
            }
            ingredientView.decrease_ingredient_value_button.setOnClickListener {
                val ingredientValue = (ingredientView.ingredient_value.text as String).toDouble()
                ingredientView.ingredient_value.text = (ingredientValue - 1).toString()
                price -= currentIngredient.getDouble("price")
                sumPriceView.sum_dish_price_value.text = getStringPriceRepresentation(price)
                if (ingredientValue == 1.0) {
                    it.isEnabled = false
                }
            }
            ingredientView.tag = currentIngredient.getInt("id")
            ingredientListLayout.addView(ingredientView)
        }

        sumPriceView.sum_dish_price_value.text = getStringPriceRepresentation(price)
        ingredientListLayout.addView(sumPriceView)
    }

    private fun sendDishToCart() {
        val ingredients = arrayListOf<HashMap<String, Int>>()

        for (child in 0 until list_ingredients_view.childCount - 1) {
            val currentChild = list_ingredients_view.getChildAt(child)
            val childHashMap = HashMap<String, Int>()
            childHashMap["ingredient_id"] = currentChild.tag as Int
            childHashMap["weight_or_volume"] =
                (currentChild.ingredient_value.text as String).toFloat().toInt()
            childHashMap["is_liquid"] = if (currentChild.ingredient_measure.text ==
                resources.getString(R.string.dish_volume_measure)) 1 else 0
            ingredients.add(childHashMap)
        }

        val requestData = HashMap<String, Any>()
        requestData["ingredients"] = ingredients
        requestData["price"] = price
        requestData["dish"] = dishId
        requestData["catering_establishment"] = 2

        val requestBody = JSONObject(requestData as Map<*, *>)

        val req: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.POST, BuildConfig.API_URL + BuildConfig.CART, requestBody,
                Response.Listener { response ->
                    AlertFragment("Дану страву із обраним складом було додано до кошика.")
                        .show(supportFragmentManager, "success")
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


    private fun getIngredientPriceSum(response: JSONArray): Double {
        var sum = 0.0
        for (i in 0 until response.length()) {
            val ingredientPrice = (response[i] as JSONObject).getDouble("price")
            val ingredientValue = (response[i] as JSONObject).getDouble("weight_or_volume")
            sum += ingredientPrice * ingredientValue
        }
        return sum
    }

    private fun getAllDishReports() {
        val url: String = BuildConfig.API_URL +
                BuildConfig.ALL_DISH_REPORTS + "$dishId/"

        val req: JsonArrayRequest =
            object : JsonArrayRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    fillReportList(response)
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

    private fun fillReportList(response: JSONArray) {
        val reportListLayout: LinearLayout = list_reports_view
        val inflater: LayoutInflater = LayoutInflater.from(this)

        for (i in 0 until response.length()) {
            val reportView = inflater.inflate(
                R.layout.view_single_report,
                reportListLayout, false)
            val currentIngredient = response[i] as JSONObject
            reportView.report_title.text = currentIngredient.getString("user")
            val reportDate = currentIngredient.getString("publication_date")
            val year = reportDate.substring(0, 4)
            val month = reportDate.substring(5, 7)
            val day = reportDate.substring(8, 10)
            reportView.report_date.text =
                if (currentLocale == "en")
                    "$month/$day/$year"
                else
                    "$day.$month.$year"
            reportView.report_text.text = currentIngredient.getString("text")
            reportListLayout.addView(reportView)
        }

        val sendReportView = inflater.inflate(
            R.layout.view_send_report,
            reportListLayout, false)
        sendReportView.send_report_button.setOnClickListener {
            sendReport(sendReportView)
        }

        reportListLayout.addView(sendReportView)
    }

    private fun sendReport(sendReportView: View) {
        val url: String = BuildConfig.API_URL + BuildConfig.SEND_NEW_REPORT

        val requestBodyMap = HashMap<String,String>()
        requestBodyMap["dish"] = dishId.toString()
        requestBodyMap["text"] = sendReportView.send_report_text.text.toString()
        val requestBody = JSONObject(requestBodyMap as Map<*, *>)

        val req: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                Response.Listener { response ->
                    list_reports_view.removeAllViews()
                    getAllDishReports()
                },
                Response.ErrorListener { error ->
                    list_reports_view.removeAllViews()
                    getAllDishReports()
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

    private fun setExpandingListener(button: Button, expandedView: ConstraintLayout) {
        button.setOnClickListener {
            if (expandedView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(
                    component_description_card, AutoTransition()
                )
                expandedView.visibility = View.VISIBLE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                TransitionManager.beginDelayedTransition(
                    component_description_card, AutoTransition()
                )
                expandedView.visibility = View.GONE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
    }
}