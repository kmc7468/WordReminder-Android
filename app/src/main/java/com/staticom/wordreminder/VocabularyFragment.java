package com.staticom.wordreminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.MeaningsAdapter;
import com.staticom.wordreminder.adapter.SelectableAdapter;
import com.staticom.wordreminder.adapter.WordsAdapter;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class VocabularyFragment extends Fragment {

    private TextView wordsText;
    private String wordsTextFormat;
    private RecyclerView words;
    private final WordsAdapter wordsAdapter = new WordsAdapter(null);
    private SelectableAdapter.OnItemSelectedListener onWordSelectedListener;

    private TextView meaningsText;
    private String defaultMeaningsText, meaningsTextFormat;
    private final MeaningsAdapter meaningsAdapter = new MeaningsAdapter(null);
    private SelectableAdapter.OnItemSelectedListener onMeaningSelectedListener;

    private void updateCount() {
        if (wordsText == null) return;

        wordsText.setText(HtmlCompat.fromHtml(
                String.format(wordsTextFormat,
                        wordsAdapter.getVocabulary().getName(),
                        wordsAdapter.getVocabulary().getVocabulary().getWords().size()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (wordsAdapter.getSelectedIndex() != -1) {
            meaningsText.setText(HtmlCompat.fromHtml(
                    String.format(meaningsTextFormat,
                            wordsAdapter.getVocabulary().getName(),
                            getSelectedWord().getWord(),
                            getSelectedWord().getMeanings().size()),
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            meaningsText.setText(HtmlCompat.fromHtml(defaultMeaningsText, HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_vocabulary, container, false);

        wordsText = view.findViewById(R.id.wordsText);
        words = view.findViewById(R.id.words);

        final RecyclerView meanings = view.findViewById(R.id.meanings);

        words.setLayoutManager(new LinearLayoutManager(view.getContext()));
        words.setAdapter(wordsAdapter);

        wordsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(words, view.findViewById(R.id.emptyWordsText)));
        wordsAdapter.setOnItemSelectedListener((v, index) -> {
            meaningsAdapter.setWord(getSelectedWord());
            meaningsAdapter.setSelectedIndex(-1);

            updateCount();

            if (onWordSelectedListener != null) {
                onWordSelectedListener.onItemSelected(v, index);
            }
        });

        meaningsText = view.findViewById(R.id.meaningsText);

        meanings.setLayoutManager(new LinearLayoutManager(view.getContext()));
        meanings.setAdapter(meaningsAdapter);

        meaningsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(meanings, view.findViewById(R.id.emptyMeaningsText)));
        meaningsAdapter.setOnItemSelectedListener((v, index) -> {
            if (onMeaningSelectedListener != null) {
                onMeaningSelectedListener.onItemSelected(v, index);
            }
        });

        onRestoreInstanceState(savedInstanceState);
        updateCount();

        return view;
    }

    public VocabularyMetadata getVocabulary() {
        return wordsAdapter.getVocabulary();
    }

    public void setVocabulary(VocabularyMetadata vocabulary) {
        Word selectedWord = null;

        if (getSelectedWordIndex() != -1) {
            selectedWord = getSelectedWord();
        }

        wordsAdapter.setVocabulary(vocabulary);

        if (selectedWord != null) {
            if (vocabulary.getVocabulary().getWords().contains(selectedWord)) {
                final int selectedWordNewIndex = vocabulary.getVocabulary().getWords().indexOf(selectedWord);
                final int selectedMeaningIndex = getSelectedMeaningIndex();

                words.scrollToPosition(selectedWordNewIndex);

                wordsAdapter.setSelectedIndex(selectedWordNewIndex);
                meaningsAdapter.setSelectedIndex(selectedMeaningIndex);
            } else {
                wordsAdapter.setSelectedIndex(-1);

                meaningsAdapter.setWord(null);
            }
        }

        updateCount();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        wordsTextFormat = savedInstanceState.getString("wordsTextFormat");
        defaultMeaningsText = savedInstanceState.getString("defaultMeaningsText");
        meaningsTextFormat = savedInstanceState.getString("meaningsTextFormat");

        setVocabulary(VocabularyMetadata.deserialize(savedInstanceState.getSerializable("vocabulary")));

        final int selectedWord = savedInstanceState.getInt("selectedWord");
        if (selectedWord != -1) {
            wordsAdapter.setSelectedIndex(selectedWord);
        }

        final int meaningIndex = savedInstanceState.getInt("selectedMeaning");
        if (meaningIndex != -1) {
            meaningsAdapter.setSelectedIndex(meaningIndex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable("vocabulary", getVocabulary().serialize());

        savedInstanceState.putString("wordsTextFormat", wordsTextFormat);
        savedInstanceState.putString("defaultMeaningsText", defaultMeaningsText);
        savedInstanceState.putString("meaningsTextFormat", meaningsTextFormat);

        savedInstanceState.putInt("selectedWord", getSelectedWordIndex());
        savedInstanceState.putInt("selectedMeaning", getSelectedMeaningIndex());
    }

    public void setOnWordSelectedListener(SelectableAdapter.OnItemSelectedListener onWordSelectedListener) {
        this.onWordSelectedListener = onWordSelectedListener;
    }

    public int getSelectedWordIndex() {
        return wordsAdapter.getSelectedIndex();
    }

    public Word getSelectedWord() {
        return wordsAdapter.getSelectedWord();
    }

    public void setSelectedWord(int index) {
        wordsAdapter.setSelectedIndex(index);
    }

    public void setSelectedWordAndScroll(int index) {
        setSelectedWord(index);

        words.scrollToPosition(index);
    }

    public void notifyWordAdded() {
        wordsAdapter.notifyItemInserted(wordsAdapter.getItemCount());

        updateCount();
    }

    public void notifySelectedWordUpdated() {
        wordsAdapter.notifyItemChanged(getSelectedWordIndex());

        updateCount();
    }

    public void notifySelectedWordRemoved() {
        wordsAdapter.notifyItemRemoved(getSelectedWordIndex());
        wordsAdapter.setSelectedIndex(-1);

        meaningsAdapter.setWord(null);
        meaningsAdapter.setSelectedIndex(-1);

        updateCount();
    }

    public void setWordsTextFormat(String wordsTextFormat) {
        this.wordsTextFormat = wordsTextFormat;

        updateCount();
    }

    public void setOnMeaningSelectedListener(SelectableAdapter.OnItemSelectedListener onMeaningSelectedListener) {
        this.onMeaningSelectedListener = onMeaningSelectedListener;
    }

    public int getSelectedMeaningIndex() {
        return meaningsAdapter.getSelectedIndex();
    }

    public Meaning getSelectedMeaning() {
        return meaningsAdapter.getSelectedMeaning();
    }

    public void setSelectedMeaning(int index) {
        meaningsAdapter.setSelectedIndex(index);
    }

    public void notifyMeaningAdded() {
        meaningsAdapter.notifyItemInserted(meaningsAdapter.getItemCount());

        updateCount();
    }

    public void notifySelectedMeaningUpdated() {
        meaningsAdapter.notifyItemChanged(getSelectedMeaningIndex());
    }

    public void notifySelectedMeaningRemoved() {
        meaningsAdapter.notifyItemRemoved(getSelectedMeaningIndex());
        meaningsAdapter.setSelectedIndex(-1);

        updateCount();
    }

    public void notifyMeaningsUpdated() {
        meaningsAdapter.notifyDataSetChanged();
    }

    public void setDefaultMeaningsText(String defaultMeaningsText) {
        this.defaultMeaningsText = defaultMeaningsText;

        updateCount();
    }

    public void setMeaningsTextFormat(String meaningsTextFormat) {
        this.meaningsTextFormat = meaningsTextFormat;

        updateCount();
    }

    public void notifyVocabularyUpdated() {
        if (getSelectedWordIndex() != -1) {
            meaningsAdapter.setWord(getSelectedWord());

            notifySelectedWordUpdated();
            notifyMeaningsUpdated();
        }
    }
}