package com.staticom.wordreminder.adapter;

import android.view.View;
import android.widget.TextView;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;

public class WordsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView word;

        public ViewHolder(View view) {
            super(view);

            word = view.findViewById(R.id.word);
        }
    }

    private VocabularyMetadata vocabulary;

    public WordsAdapter(VocabularyMetadata vocabulary) {
        super(R.layout.item_word);

        this.vocabulary = vocabulary;
    }

    public VocabularyMetadata getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(VocabularyMetadata vocabulary) {
        this.vocabulary = vocabulary;

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return vocabulary != null ? vocabulary.getVocabulary().getWords().size() : 0;
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

        myViewHolder.word.setText(word.getWord());
    }
}
