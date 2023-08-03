package com.staticom.wordreminder.contracts

import androidx.annotation.StringRes

interface View {

    fun showToast(@StringRes resId: Int, duration: Int)
}