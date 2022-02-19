package com.staticom.wordreminder.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocabularyList {

    private final List<VocabularyMetadata> vocabularyList = new ArrayList<>();

    public List<VocabularyMetadata> getVocabularyList() {
        return Collections.unmodifiableList(vocabularyList);
    }

    public VocabularyMetadata getVocabulary(int index) {
        return vocabularyList.get(index);
    }

    public void addVocabulary(VocabularyMetadata vocabulary) {
        vocabularyList.add(vocabulary);
    }

    public void removeVocabulary(VocabularyMetadata vocabulary) {
        vocabularyList.remove(vocabulary);
    }

    public JSONArray saveToJSONArray() throws JSONException {
        final JSONArray array = new JSONArray();

        for (VocabularyMetadata vocabulary : vocabularyList) {
            final JSONObject object = new JSONObject();

            object.put("name", vocabulary.getName());
            object.put("path", vocabulary.getPath().getFileName().toString());
            object.put("time", vocabulary.getTime().toString());

            array.put(object);
        }

        return array;
    }

    public static VocabularyList loadFromJSONArray(JSONArray array, Path vocabularyDirectory) throws JSONException {
        final VocabularyList list = new VocabularyList();

        for (int i = 0; i < array.length(); ++i) {
            final JSONObject object = array.getJSONObject(i);
            final String name = object.getString("name");
            final String path = object.getString("path");
            final String time = object.getString("time");

            list.addVocabulary(new VocabularyMetadata(
                    name,
                    vocabularyDirectory.resolve(path),
                    LocalDateTime.parse(time)));
        }

        return list;
    }
}