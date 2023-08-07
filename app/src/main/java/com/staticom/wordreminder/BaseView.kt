package com.staticom.wordreminder

import androidx.annotation.StringRes

interface BaseView {

    fun showInfoToast(@StringRes resId: Int)
    fun showErrorToast(@StringRes resId: Int)
}