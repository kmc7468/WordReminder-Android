package com.staticom.wordreminder.utility;

import android.widget.Spinner;

import com.staticom.wordreminder.adapter.CheckableAdapter;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.Vocabulary;

import java.util.List;

public class TagsSpinner {

    public static CheckableAdapter initializeTags(Spinner tags, Vocabulary vocabulary, String defaultTitleText, String titleTextFormat) {
        tags.setFocusable(true);
        tags.setFocusableInTouchMode(true);
        tags.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                tags.performClick();
            }
        });

        final List<Tag> tagList = vocabulary.getTags();
        final String[] tagNames = new String[tagList.size()];

        for (int i = 0; i < tagList.size(); ++i) {
            tagNames[i] = tagList.get(i).getTag();
        }

        final CheckableAdapter adapter = new CheckableAdapter(defaultTitleText, titleTextFormat, tagNames);

        tags.setAdapter(adapter);

        return adapter;
    }
}