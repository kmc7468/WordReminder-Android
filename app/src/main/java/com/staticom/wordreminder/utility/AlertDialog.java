package com.staticom.wordreminder.utility;

import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.StringRes;

public class AlertDialog {

    public interface OnButtonClick {
        void onButtonClick();
    }

    private final Context context;
    private final androidx.appcompat.app.AlertDialog.Builder builder;
    private androidx.appcompat.app.AlertDialog dialog;

    private EditText edit;

    private OnButtonClick onPositiveButtonClick, onNegativeButtonClick;

    public AlertDialog(Context context, @StringRes int titleId, @StringRes int messageId) {
        this.context = context;
        builder = new androidx.appcompat.app.AlertDialog.Builder(context);

        builder.setTitle(titleId);
        builder.setMessage(messageId);
    }

    public void addEdit(String text) {
        edit = new EditText(context);

        edit.setText(text);
        edit.setSingleLine(true);
        edit.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).callOnClick();

                return true;
            } else return false;
        });

        builder.setView(edit);
    }

    public String getEditText() {
        return edit.getText().toString();
    }

    public AlertDialog setPositiveButton(@StringRes int textId, boolean dismiss, OnButtonClick onPositiveButtonClick) {
        builder.setPositiveButton(textId, null);

        this.onPositiveButtonClick = () -> {
            onPositiveButtonClick.onButtonClick();

            if (dismiss) {
                dialog.dismiss();
            }
        };

        return this;
    }

    public AlertDialog setNegativeButton(@StringRes int textId) {
        builder.setNegativeButton(textId, null);

        this.onNegativeButtonClick = () -> {
            dialog.dismiss();
        };

        return this;
    }

    public AlertDialog setNegativeButton(@StringRes int textId, boolean dismiss, OnButtonClick onNegativeButtonClick) {
        builder.setNegativeButton(textId, null);

        this.onNegativeButtonClick = () -> {
            onNegativeButtonClick.onButtonClick();

            if (dismiss) {
                dialog.dismiss();
            }
        };

        return this;
    }

    public AlertDialog setNeutralButton(@StringRes int textId) {
        builder.setNeutralButton(textId, null);

        return this;
    }

    public void show() {
        dialog = builder.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            onPositiveButtonClick.onButtonClick();
        });
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
            onNegativeButtonClick.onButtonClick();
        });
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    public void dismiss() {
        dialog.dismiss();
    }
}