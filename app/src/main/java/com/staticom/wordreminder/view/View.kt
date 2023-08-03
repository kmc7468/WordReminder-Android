package com.staticom.wordreminder.view

import androidx.annotation.StringRes

interface View {

    fun showToast(@StringRes resId: Int, duration: Int)
}