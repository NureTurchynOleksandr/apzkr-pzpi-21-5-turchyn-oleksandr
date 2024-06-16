package com.example.foodautoplacer.auxiliary_tools

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

class RequestManager {
    fun sendJsonObjectRequest(
        url: String,
        queue: RequestQueue?,
        method: Int,
        requestBody: JSONObject?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>
    ) {

        val req = JsonObjectRequest(method, url, requestBody,
            Response.Listener { response ->
                successFunction(response as Any, successParams)
            }, Response.ErrorListener { error ->
                failFunction(error, failParams)
            })
        queue?.add(req)
    }

    fun sendJsonArrayRequest(
        url: String,
        queue: RequestQueue?,
        method: Int,
        requestBody: JSONArray?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>
    ) {
        val req = JsonArrayRequest(method, url, requestBody,
            Response.Listener { response ->
                successFunction(response as Any, successParams)
            }, Response.ErrorListener { error ->
                failFunction(error, failParams)
            })
        queue?.add(req)
    }

    fun sendJsonObjectPostRequest(
        url: String,
        params: HashMap<String, String>,
        queue: RequestQueue?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>
    ) {

        val jsonObject = JSONObject(params as Map<*, *>)

        sendJsonObjectRequest(url, queue, Request.Method.POST,
            jsonObject, successFunction, successParams, failFunction, failParams)
    }

    fun sendJsonObjectGetRequest(
        url: String,
        queue: RequestQueue?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>
    ) {

        sendJsonObjectRequest(url, queue, Request.Method.GET,
            null, successFunction, successParams, failFunction, failParams)
    }

    fun sendJsonArrayGetRequest(
        url: String,
        queue: RequestQueue?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>
    ) {

        sendJsonArrayRequest(url, queue, Request.Method.GET,
            null, successFunction, successParams, failFunction, failParams)
    }
}