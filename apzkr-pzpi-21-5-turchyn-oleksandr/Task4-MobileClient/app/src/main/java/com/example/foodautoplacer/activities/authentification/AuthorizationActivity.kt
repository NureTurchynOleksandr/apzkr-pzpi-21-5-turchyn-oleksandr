package com.example.foodautoplacer.activities.authentification

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.foodautoplacer.BuildConfig
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.basic.ActivityWithoutTopMenu
import com.example.foodautoplacer.activities.catalog.DishCatalog
import com.example.foodautoplacer.activities.dishordering.CartActivity
import com.example.foodautoplacer.activities.dishordering.OrderActivity
import com.example.foodautoplacer.activities.search.CateringEstablishmentSearchActivity
import kotlinx.android.synthetic.main.activity_authorization.*
import org.json.JSONObject

class AuthorizationActivity : ActivityWithoutTopMenu() {
    private val TAG = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)
        requestQueue = Volley.newRequestQueue(this)
        verifyAccessToken()

        Log.d(TAG, BuildConfig.API_URL)
        Log.d(TAG, BuildConfig.CATALOG_FILTERS)
    }
    fun openRegistrationForm(view: View) {
        startNewActivity(RegistrationActivity::class.java)
    }

    fun authorize(view: View) {
        val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        sendAuthorizationRequest(prefs, edit)
    }

    private fun verifyAccessToken() {
        val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", "").toString()
        val url = BuildConfig.API_URL + BuildConfig.ACCESS_TOKEN_VERIFICATION
        val edit = prefs.edit()
        val params = HashMap<String,String>()
        params["token"] = accessToken

        requestManager.sendJsonObjectPostRequest(
            url,
            params,
            requestQueue,
            this::executeIfTokenVerificationIsSuccessful,
            arrayListOf(),
            this::executeIfTokenVerificationIsFailed,
            arrayListOf(prefs, edit),
        )
    }

    private fun executeIfTokenVerificationIsSuccessful(
        response: Any,
        args: ArrayList<Any?>
    ) {
        startNewActivity(DishCatalog::class.java)
    }

    private fun executeIfTokenVerificationIsFailed(
        error: VolleyError,
        args: ArrayList<Any?>) {
        if (args[1] != null) {
            refreshJWT(args[0] as SharedPreferences,
                args[1] as SharedPreferences.Editor?
            )
        }
    }

    private fun refreshJWT(prefs: SharedPreferences,
                           edit: SharedPreferences.Editor?) {
        val url = BuildConfig.API_URL + BuildConfig.ACCESS_TOKEN_VERIFICATION
        val params = HashMap<String,String>()
        params["refresh"] = prefs.getString(
            "refresh_token", "").toString()

        requestManager.sendJsonObjectPostRequest(url, params, requestQueue,
            this::executeIfJwtRefreshIsSuccessful,
            arrayListOf(prefs, edit),
            this::executeIfJwtRefreshIsFailed,
            arrayListOf()
        )
    }

    private fun executeIfJwtRefreshIsSuccessful(
        response: Any,
        args: ArrayList<Any?>) {
        setNewAccessTokenValue(response as JSONObject, args)
    }

    private fun setNewAccessTokenValue(
        response: JSONObject,
        args: ArrayList<Any?>) {
        val edit: SharedPreferences.Editor = args[1] as SharedPreferences.Editor
        edit.putString("access_token", response["access"].toString())
        edit.commit()
    }

    private fun executeIfJwtRefreshIsFailed(
        response: Any,
        args: ArrayList<Any?>) {
    }

    private fun sendAuthorizationRequest(prefs: SharedPreferences,
                                         edit: SharedPreferences.Editor) {
        val url = BuildConfig.API_URL + BuildConfig.LOGIN_API
        val params = HashMap<String,String>()
        params["username"] = usernameAuthorizationEdit.text.toString()
        params["password"] = passwordAuthorizationEdit.text.toString()

        requestManager.sendJsonObjectPostRequest(
            url,
            params,
            requestQueue,
            this::executeIfAuthorizationIsSuccessful,
            arrayListOf(prefs, edit),
            this::executeIfAuthorizationIsFailed,
            arrayListOf(),
        )
    }

    private fun executeIfAuthorizationIsSuccessful(
        response: Any,
        args: ArrayList<Any?>) {
        val edit: SharedPreferences.Editor = args[1] as SharedPreferences.Editor
        val responseJsonObject = response as JSONObject
        edit.putString("access_token", responseJsonObject["access"].toString())
        edit.putString("refresh_token", responseJsonObject["refresh"].toString())
        edit.commit()
        startNewActivity(DishCatalog::class.java)
    }

    private fun executeIfAuthorizationIsFailed(
        error: VolleyError,
        args: ArrayList<Any?>) {
        auth_error.visibility = View.VISIBLE
    }

}