package com.staticom.wordreminder;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.staticom.wordreminder.adapter.VocabularyListAdapter;
import com.staticom.wordreminder.core.VocabularyList;
import com.staticom.wordreminder.core.VocabularyMetadata;

import org.json.JSONArray;

import java.nio.file.Files;
import java.nio.file.Path;

public class MainActivity extends AppCompatActivity {

    private Path rootPath;

    private VocabularyList vocabularyList;
    private VocabularyListAdapter vocabularyListAdapter;
    private VocabularyMetadata selectedVocabulary;

    private FloatingActionButton create, load;
    private boolean isOpenAddButtons = false;
    private Animation fabOpen, fabClose;

    private boolean readVocabularyList() {
        try {
            final Path path = rootPath.resolve("vocabularyList.json");
            if (Files.exists(path)) {
                final byte[] bytes = Files.readAllBytes(path);
                final JSONArray array = new JSONArray(new String(bytes));

                vocabularyList = VocabularyList.loadFromJSONArray(array, rootPath);
            } else {
                vocabularyList = new VocabularyList();
            }

            return true;
        } catch (Exception e) {
            vocabularyList = null;

            Toast.makeText(this, getString(R.string.main_activity_error_read_vocabulary_list), Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return false;
        }
    }

    private void setSelectedVocabulary(VocabularyMetadata vocabulary) {
        selectedVocabulary = vocabulary;

        // TODO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.main_activity_title));

        rootPath = getFilesDir().toPath();

        if (readVocabularyList()) {
            vocabularyListAdapter = new VocabularyListAdapter(vocabularyList);

            vocabularyListAdapter.setOnItemSelectedListener((view, index) -> {
                setSelectedVocabulary(vocabularyList.getVocabulary(index));
            });
            vocabularyListAdapter.setOnEditButtonClickListener(index -> {
                // TODO
            });

            final RecyclerView vocabularyList = findViewById(R.id.vocabularyList);

            vocabularyList.setLayoutManager(new LinearLayoutManager(this));
            vocabularyList.setAdapter(vocabularyListAdapter);
        }

        create = findViewById(R.id.create);
        load = findViewById(R.id.load);

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
    }

    private void toggleAddButtons() {
        if (isOpenAddButtons) {
            load.startAnimation(fabClose);
            create.startAnimation(fabClose);
        } else {
            load.startAnimation(fabOpen);
            create.startAnimation(fabOpen);
        }

        isOpenAddButtons = !isOpenAddButtons;

        create.setVisibility(isOpenAddButtons ? View.VISIBLE : View.INVISIBLE);
        create.setClickable(isOpenAddButtons);

        load.setVisibility(isOpenAddButtons ? View.VISIBLE : View.INVISIBLE);
        load.setClickable(isOpenAddButtons);
    }

    public void onCreateClick(View view) {
        toggleAddButtons();

        // TODO
    }

    public void onLoadClick(View view) {
        toggleAddButtons();

        // TODO
    }

    public void onAddClick(View view) {
        toggleAddButtons();
    }
}