package com.example.foodautoplacer.activities.authentification

import android.os.Bundle
import android.view.View
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.foodautoplacer.BuildConfig
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.basic.ActivityWithoutTopMenu
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_registration. usernameRegistrationEdit

class RegistrationActivity : ActivityWithoutTopMenu() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        requestQueue = Volley.newRequestQueue(this)
    }

    fun openAuthorizationForm(view: View) {
        startNewActivity(AuthorizationActivity::class.java)
    }

    fun sendRegistrationRequest(view: View) {
        val url = BuildConfig.API_URL + BuildConfig.REGISTRATION_API
        val params = HashMap<String,String>()
        params["username"] = usernameRegistrationEdit.text.toString()
        params["password"] = passwordRegistrationEdit.text.toString()
        params["re_password"] = rePasswordRegisterEdit.text.toString()

        requestManager.sendJsonObjectPostRequest(
            url,
            params,
            requestQueue,
            this::executeIfRegistrationIsSuccessful,
            arrayListOf(),
            this::executeIfRegistrationIsFailed,
            arrayListOf(),
        )
    }

    private fun executeIfRegistrationIsSuccessful(
        response: Any,
        args: ArrayList<Any?>) {
        startNewActivity(AuthorizationActivity::class.java)
    }

    private fun executeIfRegistrationIsFailed(
        error: VolleyError,
        args: ArrayList<Any?>) {
    }
}