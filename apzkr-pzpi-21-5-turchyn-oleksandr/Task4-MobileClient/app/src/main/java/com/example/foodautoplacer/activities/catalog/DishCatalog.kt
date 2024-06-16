package com.example.foodautoplacer.activities.catalog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.foodautoplacer.BuildConfig
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.basic.ActivityWithTopMenu
import com.example.foodautoplacer.auxiliary_tools.DishComparator
import com.example.foodautoplacer.dataclasses.DishCatalogItem
import com.example.foodautoplacer.fragments.CatalogFiltrationFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dish_catalog.*
import org.json.JSONArray

class DishCatalog : ActivityWithTopMenu() {
    private var establishmentId = 2
    private val establishmentDishes: ArrayList<DishCatalogItem> = arrayListOf()
    private val chosenDishes: ArrayList<DishCatalogItem> = arrayListOf()
    val types: MutableSet<String> = mutableSetOf()
    val popularities: MutableSet<String> = mutableSetOf()
    val chosenTypes: MutableSet<String> = mutableSetOf()
    val chosenPopularities: MutableSet<String> = mutableSetOf()
    private lateinit var dishRecyclerView: RecyclerView
    private var dishAdapter: DishAdapter? = null
    var minRate: Float = 1.0F
    var maxRate: Float = 5F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_catalog)
        requestQueue = Volley.newRequestQueue(this)
        dishRecyclerView = catalog_recycler_view
        dishRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        fillSortingSpinner()
        setSortingSpinnerItemsListener()
        setSearchRequestListener()

        getEstablishmentDishes()
    }

    private fun getEstablishmentDishes() {
        val url: String = BuildConfig.API_URL + BuildConfig.ESTABLISHMENT_DISHES +
                establishmentId.toString() + '/'

        val req: JsonArrayRequest =
            object : JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    saveEstablishmentDishes(response)
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

    private fun saveEstablishmentDishes(dishArray: JSONArray) {
        for (i in 0 until dishArray.length()) {
            val currentDish = dishArray.getJSONArray(i)
            establishmentDishes.add(DishCatalogItem(
                    currentDish[0] as Int,
                    currentDish[1] as String,
                    currentDish[2] as String,
                    currentDish[3] as String,
                    currentDish[4] as String,
                    currentDish[5] as String,
                    currentDish[6] as Int
            ))
            types.add(currentDish[4] as String)
            val currentPopularity = currentDish[5] as String
            if (currentPopularity != "") {
                popularities.add(currentPopularity)
            }
        }
        establishmentDishes.sortWith(DishComparator.nameComparator)
        fillAppropriateDishContainer()
    }

    fun fillAppropriateDishContainer() {
        filterDishList()
        dishAdapter = DishAdapter(chosenDishes)
        dishRecyclerView.adapter = dishAdapter
    }

    private fun filterDishList() {
        chosenDishes.clear()
        val componentSearchCriteria =
            searchView.query.toString().toLowerCase()
        for (dish in establishmentDishes) {
            if (
                (dish.type in chosenTypes || chosenTypes.size == 0) &&
                (dish.popularity in chosenPopularities || chosenPopularities.size == 0) &&
                (dish.rate >= minRate && dish.rate <= maxRate) &&
                (dish.name.toLowerCase().indexOf(componentSearchCriteria) != -1 ||
                        componentSearchCriteria.isEmpty())) {
                chosenDishes.add(dish)
            }
        }
    }

    fun setSortingSpinnerItemsListener() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> establishmentDishes.sortWith(DishComparator.nameComparator)
                    1 -> establishmentDishes.sortWith(DishComparator.rateAscendingComparator)
                    2 -> establishmentDishes.sortWith(DishComparator.rateDescendingComparator)
                }
                fillAppropriateDishContainer()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
    }

    private fun setSearchRequestListener() {
        searchView.setOnQueryTextListener (object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                fillAppropriateDishContainer()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                fillAppropriateDishContainer()
                return false
            }
        })
    }

    private inner class DishHolder(view: View)
        : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = itemView.findViewById(R.id.dish_catalog_name)
        val typeTextView: TextView = itemView.findViewById(R.id.dish_catalog_type)
        val ratingView: RatingBar = itemView.findViewById(R.id.dish_catalog_rating_bar)
        val descriptionTextView: TextView = itemView.findViewById(R.id.dish_catalog_description)
        val popularityTextView: TextView = itemView.findViewById(R.id.dish_catalog_popularity)
        val imageView: ImageView = itemView.findViewById(R.id.dish_catalog_image)
    }

    private inner class DishAdapter(var dishes: ArrayList<DishCatalogItem>)
        : RecyclerView.Adapter<DishHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishHolder {
            val view = layoutInflater.inflate(R.layout.view_dish_list_item, parent, false)
            return DishHolder(view)
        }

        override fun getItemCount(): Int {
            return dishes.size
        }

        override fun onBindViewHolder(holder: DishHolder, position: Int) {
            val dish = dishes[position]
            holder.apply {
                nameTextView.text = dish.name
                nameTextView.tag = dish.id
                typeTextView.text = dish.type
                descriptionTextView.text = dish.description
                popularityTextView.text = dish.popularity
                if (dish.popularity == "") {
                    popularityTextView.visibility = View.INVISIBLE
                }
                ratingView.rating = dish.rate.toFloat()
                val imageLink = BuildConfig.API_URL +
                        BuildConfig.IMAGES + dish.image
                Picasso.get().load(imageLink).into(imageView)
                imageView.tag = dish.id
            }
        }
    }

    private fun fillSortingSpinner() {
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add(resources.getString(R.string.sort_by_name))
        arrayList.add(resources.getString(R.string.sort_by_rating_asc))
        arrayList.add(resources.getString(R.string.sort_by_rating_desc))
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
    }

    fun showFilterDialog(view: View) {
        val filterAlert =
            CatalogFiltrationFragment(
                this
            )
        filterAlert.show(supportFragmentManager, "filtration")
    }

    fun openDishActivity(view: View) {
        val componentIntent = Intent(this,
            DishActivity::class.java)
            componentIntent.putExtra("dishId", view.tag as Int)
        startActivity(componentIntent)
    }
}