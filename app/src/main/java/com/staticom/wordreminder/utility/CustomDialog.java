package com.staticom.wordreminder.utility;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;

public class CustomDialog {

    private final Dialog dialog;

    public CustomDialog(Context context, @LayoutRes int contentId) {
        dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(contentId);
    }

    public <T extends View> T findViewById(int id) {
        return dialog.findViewById(id);
    }

    public void show() {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        params.copyFrom(window.getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(params);

        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}