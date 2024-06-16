package com.example.foodautoplacer.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager


class AlertFragment(messageText: String) : DialogFragment() {
    private var messageToShow = messageText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
//            params.setMargins(20, 10, 20, 10)

            val alertBody = LinearLayout(context)
            alertBody.layoutParams = params
            val messageBox = TextView(context)
            messageBox.text = messageToShow
            messageBox.setPadding(30, 10, 30, 10)
            alertBody.addView(messageBox)

            builder.setTitle("Увага")
                .setView(alertBody)
                .setPositiveButton("Ok") { _, _ ->
                }
            val alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.textSize = 17F
            }

            return alert
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}