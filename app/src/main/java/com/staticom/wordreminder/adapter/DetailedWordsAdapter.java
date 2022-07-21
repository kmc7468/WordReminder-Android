package com.staticom.wordreminder.adapter;

import android.view.View;
import android.widget.TextView;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;

public class DetailedWordsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView word;
        private final TextView meanings;
        private final TextView examples;
        private final TextView pronunciations;

        public ViewHolder(View view) {
            super(view);

            word = view.findViewById(R.id.word);
            meanings = view.findViewById(R.id.meanings);
            examples = view.findViewById(R.id.examples);
            pronunciations = view.findViewById(R.id.pronunciations);
        }
    }

    private VocabularyMetadata vocabulary;

    public DetailedWordsAdapter(VocabularyMetadata vocabulary) {
        super(R.layout.item_detailed_word);

        this.vocabulary = vocabulary;
    }

    public VocabularyMetadata getVocabulary() {
        return vocabulary;
    }

    @Override
    public int getItemCount() {
        return vocabulary.getVocabulary().getWords().size();
    }

    public Word getSelectedWord() {
        return vocabulary.getVocabulary().getWord(getSelectedIndex());
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final ViewHolder myViewHolder = (ViewHolder)viewHolder;
        final Word word = vocabulary.getVocabulary().getWord(position);
        final Meaning mergedMeaning = word.mergeMeanings(", ", ", ", "\n");

        myViewHolder.word.setText(word.getWord());
        myViewHolder.meanings.setText(mergedMeaning.getMeaning());

        if (mergedMeaning.hasPronunciation()) {
            myViewHolder.pronunciations.setVisibility(View.VISIBLE);
            myViewHolder.pronunciations.setText("[" + mergedMeaning.getPronunciation() + "]");
        } else {
            myViewHolder.pronunciations.setVisibility(View.GONE);
        }

        if (mergedMeaning.hasExample()) {
            myViewHolder.examples.setVisibility(View.VISIBLE);
            myViewHolder.examples.setText(mergedMeaning.getExample());
        } else {
            myViewHolder.examples.setVisibility(View.GONE);
        }
    }
}
