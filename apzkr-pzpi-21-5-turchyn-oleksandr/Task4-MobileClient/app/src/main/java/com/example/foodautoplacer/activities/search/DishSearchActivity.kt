package com.example.foodautoplacer.activities.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.foodautoplacer.BuildConfig
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.basic.ActivityWithTopMenu
import com.example.foodautoplacer.auxiliary_tools.SearchAdapter
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.activity_dish_search.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class DishSearchActivity : ActivityWithTopMenu() {
    private val ingredients: ArrayList<Pair<String, Int>> = arrayListOf(Pair("", -1))
    private val colorSequence: ArrayList<Int> = arrayListOf(
        R.color.pink, R.color.green, R.color.blue,
        R.color.dark_blue, R.color.orange, R.color.purple
    )
    private val appropriateDishes: ArrayList<Pair<String, String>> = arrayListOf()
    private val almostAppropriateDishes: ArrayList<Pair<String, String>> = arrayListOf()
    private val selectedIngredients = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_search)

        requestQueue = Volley.newRequestQueue(this)

        getIngredients()
        initializeSpinnerSelectionListener()

        search_dishes_button.setOnClickListener {
            getAppropriateDishes()
        }
    }

    private fun getIngredients() {
        val url = BuildConfig.API_URL + BuildConfig.INGREDIENTS

        val req: JsonArrayRequest =
            object : JsonArrayRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    saveIngredients(response)
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

    private fun saveIngredients(response: JSONArray) {
        for (i in 0 until response.length()) {
            val currentIngredient = response[i] as JSONObject
            ingredients.add(
                Pair(
                    currentIngredient.getString("name"),
                    currentIngredient.getInt("id")
                )
            )
        }
        fillIngredientAdapter()
    }

    private fun fillIngredientAdapter() {
        val ingredientAdapter =
            SearchAdapter(
                this,
                android.R.layout.simple_spinner_item, ingredients
            )
        ingredientAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        search_ingredients_ingredient_spinner.adapter = ingredientAdapter
    }

    private fun initializeSpinnerSelectionListener() {
        search_ingredients_ingredient_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View,
                    position: Int,
                    id: Long
                ) {
                    if (id != -1L && !selectedIngredients.contains(id.toInt())) {
                        addIngredientButtonToFlexbox(
                            search_ingredients_ingredient_spinner.selectedItem.toString(),
                            id.toInt()
                        )
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                }
            }
    }

    private fun addIngredientButtonToFlexbox(
        buttonText: String, buttonTag: Int) {
        val params: FlexboxLayoutManager.LayoutParams =
            FlexboxLayoutManager.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        params.setMargins(10, 5, 10, 5)

        val newButton = TextView(applicationContext)
        newButton.layoutParams = params
        newButton.text = buttonText
        newButton.textSize = 18F
        newButton.setTextColor(
            resources.getColor(R.color.white)
        )
        newButton.setPadding(30, 10, 30, 10)
        val buttonColor: Int = colorSequence.random()
        newButton.setBackgroundResource(buttonColor)
        newButton.setOnClickListener { view ->
            val parentView = view.parent as ViewGroup
            parentView.removeView(view)
            selectedIngredients.remove(buttonTag)
        }
        newButton.tag = buttonTag
        buttonContainer.addView(newButton)
        selectedIngredients.add(buttonTag)
    }

    fun emptyCanvas(view: View) {
        buttonContainer.removeAllViews()
    }

    private fun getAppropriateDishes() {
        emptyPreviousResults()

        val chosenIngredientIds = getChosenIngredientIds()
        val searchUrl = getParameterStringForAppropriateDishes(chosenIngredientIds)

        val req: JsonArrayRequest =
            object : JsonArrayRequest(Request.Method.GET, searchUrl, null,
                Response.Listener { response ->
                    processFoundDishes(response)
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

    private fun emptyPreviousResults() {
        appropriateDishes.clear()
        almostAppropriateDishes.clear()
    }

    private fun processFoundDishes(response: JSONArray) {
        saveAppropriateDishes(response)
        saveAlmostAppropriateDishes(response)
        showSearchResults()
    }

    private fun saveAppropriateDishes(response: JSONArray) {
        saveFoundDishes(response, 0, appropriateDishes)
    }

    private fun saveAlmostAppropriateDishes(response: JSONArray) {
        saveFoundDishes(response, 1, almostAppropriateDishes)
    }

    private fun saveFoundDishes(
        response: JSONArray,
        index: Int,
        dishContainer: ArrayList<Pair<String, String>>
    ) {
        val dishesArray = response[index] as JSONArray
        for (i in 0 until dishesArray.length()) {
            val currentDish = dishesArray[i] as JSONObject
            dishContainer.add(
                Pair(
                    currentDish.getString("name"),
                    currentDish.getString("image")
                )
            )
        }
    }

    private fun getChosenIngredientIds(): ArrayList<Int> {
        val idArray: ArrayList<Int> = arrayListOf()
        var currentChild: View
        for (child in 0 until buttonContainer.childCount) {
            currentChild = buttonContainer.getChildAt(child)
            idArray.add(currentChild.tag as Int)
        }
        return idArray
    }

    private fun getParameterStringForAppropriateDishes(
        ingredientIds: ArrayList<Int>
    ): String {
        val url = StringBuilder(BuildConfig.API_URL + BuildConfig.APPROPRIATE_DISHES + "?")
        for (k in 0 until ingredientIds.size) {
            url.append("0=${ingredientIds[k]}&")
        }
        url.append("1=${search_ingredients_missed_ingredient_count.text}&")
        url.deleteAt(url.length - 1)
        return url.toString()
    }

    private fun showSearchResults() {
        refreshResultItemsVisibility()
        if (appropriateDishes.size > 0) {
            fillResultSlider(appropriate_slider, appropriateDishes)
        }
        if (almostAppropriateDishes.size > 0) {
            fillResultSlider(almost_appropriate_slider, almostAppropriateDishes)
        }
    }

    private fun refreshResultItemsVisibility() {
        no_dishes_found.visibility =
            if (appropriateDishes.size == 0 && almostAppropriateDishes.size == 0)
                View.VISIBLE else View.GONE
        appropriate_dishes.visibility = if (appropriateDishes.size > 0)
            View.VISIBLE else View.GONE
        appropriate_slider.visibility = if (appropriateDishes.size > 0)
            View.VISIBLE else View.GONE
        almost_appropriate_dishes.visibility = if (almostAppropriateDishes.size > 0)
            View.VISIBLE else View.GONE
        almost_appropriate_slider.visibility = if (almostAppropriateDishes.size > 0)
            View.VISIBLE else View.GONE
    }

    private fun fillResultSlider(
        slider: ImageSlider,
        dishContainer: ArrayList<Pair<String, String>>
    ) {
        val imageList = ArrayList<SlideModel>()
        for (dish in dishContainer) {
            val dishName = dish.first
            val dishImage = BuildConfig.API_URL +
                    BuildConfig.IMAGES + dish.second
            imageList.add(SlideModel(dishImage, dishName, ScaleTypes.FIT))
        }
        slider.setImageList(imageList)
    }
}