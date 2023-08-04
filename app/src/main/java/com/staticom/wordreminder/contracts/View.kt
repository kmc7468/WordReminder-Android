package com.staticom.wordreminder.contracts

import androidx.annotation.StringRes

interface View {

    fun showInfoToast(@StringRes resId: Int)
    fun showErrorToast(@StringRes resId: Int)
}