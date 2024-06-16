package com.example.foodautoplacer.activities.basic

import android.view.MenuItem

open class ActivityWithoutTopMenu: GeneralApplicationActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        changeUserInterfaceLanguage()
        return true
    }
}