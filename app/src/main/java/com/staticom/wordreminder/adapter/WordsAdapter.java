package com.staticom.wordreminder.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;

import java.util.stream.Collectors;

public class WordsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView word;
        private final TextView relations;

        public ViewHolder(View view) {
            super(view);

            word = view.findViewById(R.id.word);
            relations = view.findViewById(R.id.relations);

            onHolderDeactivated(true);
        }

        @Override
        public void onHolderActivated(boolean isBindMode) {
            final Word word = vocabulary.getVocabulary().getWord(getAdapterPosition());

            relations.setVisibility(word.hasRelation() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onHolderDeactivated(boolean isBindMode) {
            relations.setVisibility(View.GONE);
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
        myViewHolder.relations.setText(HtmlCompat.fromHtml(
                word.getRelations().stream().map(relation -> {
                    return String.format(
                            viewHolder.itemView.getContext().getString(R.string.words_adapter_relation),
                            relation.getWord().getWord(), relation.getRelation());
                }).collect(Collectors.joining("\n")),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (myViewHolder.itemView.isActivated()) {
            myViewHolder.onHolderActivated(true);
        } else {
            myViewHolder.onHolderDeactivated(true);
        }
    }
}
