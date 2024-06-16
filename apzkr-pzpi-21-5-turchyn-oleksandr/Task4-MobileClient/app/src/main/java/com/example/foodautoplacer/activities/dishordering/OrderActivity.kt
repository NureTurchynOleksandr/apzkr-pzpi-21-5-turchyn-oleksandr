package com.example.foodautoplacer.activities.dishordering

import android.content.Context
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
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.view_order_row.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder

class OrderActivity : ActivityWithTopMenu() {
    private var establishmentId = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        requestQueue = Volley.newRequestQueue(this)

        getUserOrders()
    }

    private fun getUserOrders() {
        val url: String = BuildConfig.API_URL + BuildConfig.ORDER +
                "?catering_establishment=$establishmentId"

        val req: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    saveOrderData(response)
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

    private fun saveOrderData(response: JSONObject) {
        val orderTable: TableLayout = tableLayout2
        val inflater: LayoutInflater = LayoutInflater.from(this)

        for (k in response.keys()) {
            val currentOrder = response.getJSONArray(k)
            val currentStatusCode = currentOrder[1] as Int

            val view = inflater.inflate(
                R.layout.view_order_row,
                orderTable, false)

            when(currentStatusCode) {
                0-> view.order_item_status.text = "Очікується"
                1-> view.order_item_status.text = "В черзі"
                2-> view.order_item_status.text = "Готово"
            }

            val cookingTime = currentOrder[2] as String
            if (cookingTime.substring(0, 4) == "1970") {
                view.order_item_cook_time.text = "---"
            } else {
                view.order_item_cook_time.text = cookingTime.substring(0, 10)
            }

            val numberInQueue = currentOrder[3] as Int
            if (numberInQueue == -1) {
                view.order_item_number_in_queue.text = "---"
            } else {
                view.order_item_number_in_queue.text = numberInQueue.toString()
            }

            val orderDishes = currentOrder[0] as JSONArray
            for (i in 0 until orderDishes.length()) {
                val newDish = TextView(applicationContext)
                newDish.text = orderDishes[i] as String
                newDish.textAlignment = View.TEXT_ALIGNMENT_CENTER
                view.order_item_dishes.addView(newDish)
            }

            view.order_item_confirm_cooking.setOnClickListener {
                deleteOrder(k.toInt())
                orderTable.removeView(view)
            }

            orderTable.addView(view)
        }
    }

    private fun deleteOrder(orderNumber: Int) {
        val url: String = BuildConfig.API_URL + BuildConfig.ORDER +
                "?order=$orderNumber"

        val req: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.DELETE, url, null,
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
}