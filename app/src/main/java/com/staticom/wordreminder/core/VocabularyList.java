package com.staticom.wordreminder.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocabularyList {

    private final List<VocabularyMetadata> vocabularyList = new ArrayList<>();
    private final List<VocabularyMetadata> deletedVocabularyList = new ArrayList<>();

    public List<VocabularyMetadata> getVocabularyList() {
        return Collections.unmodifiableList(vocabularyList);
    }

    public VocabularyMetadata getVocabulary(int index) {
        return vocabularyList.get(index);
    }

    public boolean containsVocabulary(String name) {
        for (final VocabularyMetadata vocabulary : vocabularyList) {
            if (vocabulary.getName().equals(name)) return true;
        }

        return false;
    }

    public void addVocabulary(VocabularyMetadata vocabulary) {
        vocabularyList.add(vocabulary);
    }

    public void removeVocabulary(VocabularyMetadata vocabulary) {
        vocabularyList.remove(vocabulary);

        deletedVocabularyList.add(vocabulary);
    }

    public void saveOrDeleteVocabulary() throws IOException {
        for (final VocabularyMetadata vocabulary : deletedVocabularyList) {
            try {
                Files.delete(vocabulary.getPath());
            } catch (final NoSuchFileException ignored) {
            }
        }

        deletedVocabularyList.clear();

        for (final VocabularyMetadata vocabulary : vocabularyList) {
            if (vocabulary.shouldSave()) {
                vocabulary.saveVocabulary();
                vocabulary.setShouldSave(false);
            }
        }
    }

    public JSONArray saveToJSONArray() throws JSONException {
        final JSONArray array = new JSONArray();

        for (final VocabularyMetadata vocabulary : vocabularyList) {
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