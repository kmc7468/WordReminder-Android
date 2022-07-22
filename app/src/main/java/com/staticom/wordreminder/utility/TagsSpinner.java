package com.staticom.wordreminder.utility;

import android.widget.Spinner;

import com.staticom.wordreminder.adapter.CheckableAdapter;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.Vocabulary;

public class TagsSpinner {

    public static CheckableAdapter initializeTags(Spinner tags, Vocabulary vocabulary,
                                                  String defaultTitleText, String titleTextFormat) {
        final CheckableAdapter adapter = new CheckableAdapter(defaultTitleText, titleTextFormat,
                vocabulary.getTags().stream().map(Tag::getTag).toArray(String[]::new));

        tags.setFocusable(true);
        tags.setFocusableInTouchMode(true);
        tags.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                tags.performClick();
            }
        });
        tags.setAdapter(adapter);

        return adapter;
    }
}