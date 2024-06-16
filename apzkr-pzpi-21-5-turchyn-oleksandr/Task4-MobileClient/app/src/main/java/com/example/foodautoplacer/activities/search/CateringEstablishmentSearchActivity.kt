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
import kotlinx.android.synthetic.main.activity_catering_establishment_search.*
import kotlinx.android.synthetic.main.activity_dish_search.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class CateringEstablishmentSearchActivity: ActivityWithTopMenu() {
    private val dishes: ArrayList<Pair<String, Int>> = arrayListOf(Pair("", -1))
    private val colorSequence: ArrayList<Int> = arrayListOf(
        R.color.pink, R.color.green, R.color.blue,
        R.color.dark_blue, R.color.orange, R.color.purple
    )
    private val appropriateEstablishments: ArrayList<Pair<String, String>> = arrayListOf()
    private val selectedDishes = HashSet<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catering_establishment_search)

        requestQueue = Volley.newRequestQueue(this)

        getDishes()
        initializeSpinnerSelectionListener()

        search_catering_establishment_button.setOnClickListener {
            getAppropriateEstablishments()
        }
    }

    private fun getDishes() {
        val url = BuildConfig.API_URL + BuildConfig.DISH

        val req: JsonArrayRequest =
            object : JsonArrayRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    saveDishes(response)
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

    private fun saveDishes(response: JSONArray) {
        for (i in 0 until response.length()) {
            val currentIngredient = response[i] as JSONObject
            dishes.add(
                Pair(
                    currentIngredient.getString("name"),
                    currentIngredient.getInt("id")
                )
            )
        }
        fillDishAdapter()
    }

    private fun fillDishAdapter() {
        val ingredientAdapter =
            SearchAdapter(
                this,
                android.R.layout.simple_spinner_item, dishes
            )
        ingredientAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        search_catering_establishment_spinner.adapter = ingredientAdapter
    }

    private fun initializeSpinnerSelectionListener() {
        search_catering_establishment_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View,
                    position: Int,
                    id: Long
                ) {
                    if (id != -1L && !selectedDishes.contains(id.toInt())) {
                        addDishButtonToFlexbox(
                            search_catering_establishment_spinner.selectedItem.toString(),
                            id.toInt()
                        )
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                }
            }
    }

    private fun addDishButtonToFlexbox(buttonText: String, buttonTag: Int) {
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
        newButton.setTextColor(resources.getColor(R.color.white))
        newButton.setPadding(30, 10, 30, 10)
        val buttonColor: Int = colorSequence.random()
        newButton.setBackgroundResource(buttonColor)
        newButton.setOnClickListener { view ->
            val parentView = view.parent as ViewGroup
            parentView.removeView(view)
            selectedDishes.remove(buttonTag)
        }
        newButton.tag = buttonTag
        dishButtonContainer.addView(newButton)
        selectedDishes.add(buttonTag)
    }

    fun emptyCanvas(view: View) {
        dishButtonContainer.removeAllViews()
    }

    private fun getAppropriateEstablishments() {
        emptyPreviousResults()

        val chosenDishIds = getChosenDishIds()
        val searchUrl = getParameterStringForAppropriateEstablishments(chosenDishIds)

        val req: JsonArrayRequest =
            object : JsonArrayRequest(Request.Method.GET, searchUrl, null,
                Response.Listener { response ->
                    processFoundEstablishments(response)
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
        appropriateEstablishments.clear()
    }

    private fun processFoundEstablishments(response: JSONArray) {
        saveAppropriateEstablishments(response)
        showSearchResults()
    }

    private fun saveAppropriateEstablishments(response: JSONArray) {
        saveFoundEstablishments(response, 0, appropriateEstablishments)
    }

    private fun saveFoundEstablishments(
        response: JSONArray,
        index: Int,
        establishmentContainer: ArrayList<Pair<String, String>>
    ) {
        for (i in 0 until response.length()) {
            val currentEstablishment = response[i] as JSONObject
            establishmentContainer.add(
                Pair(
                    currentEstablishment.getString("name"),
                    currentEstablishment.getString("image")
                )
            )
        }
    }

    private fun getChosenDishIds(): ArrayList<Int> {
        val idArray: ArrayList<Int> = arrayListOf()
        var currentChild: View
        for (child in 0 until dishButtonContainer.childCount) {
            currentChild = dishButtonContainer.getChildAt(child)
            idArray.add(currentChild.tag as Int)
        }
        return idArray
    }

    private fun getParameterStringForAppropriateEstablishments(
        dishIds: ArrayList<Int>
    ): String {
        val url = StringBuilder(BuildConfig.API_URL + BuildConfig.APPROPRIATE_CATERING_ESTABLISHMENTS + "?")
        for (k in 0 until dishIds.size) {
            url.append("dish=${dishIds[k]}&")
        }
        url.deleteAt(url.length - 1)
        return url.toString()
    }

    private fun showSearchResults() {
        refreshResultItemsVisibility()
        if (appropriateEstablishments.size > 0) {
            fillResultSlider(appropriate_establishments_slider, appropriateEstablishments)
        }
    }

    private fun refreshResultItemsVisibility() {
        no_establishments_found.visibility =
            if (appropriateEstablishments.size == 0)
                View.VISIBLE else View.GONE
        appropriate_establishments_title.visibility = if (appropriateEstablishments.size > 0)
            View.VISIBLE else View.GONE
        appropriate_establishments_slider.visibility = if (appropriateEstablishments.size > 0)
            View.VISIBLE else View.GONE
    }

    private fun fillResultSlider(
        slider: ImageSlider,
        establishmentContainer: ArrayList<Pair<String, String>>
    ) {
        val imageList = ArrayList<SlideModel>()
        for (establishment in establishmentContainer) {
            val establishmentName = establishment.first
            val establishmentImage = establishment.second
            imageList.add(SlideModel(establishmentImage, establishmentName, ScaleTypes.FIT))
        }
        slider.setImageList(imageList)
    }
}