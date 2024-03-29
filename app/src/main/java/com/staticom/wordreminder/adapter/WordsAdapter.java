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
        }

        public void updateOptionalViewsVisibility() {
            final Word word = vocabulary.getVocabulary().getWord(getAdapterPosition());

            relations.setVisibility(word.hasRelations() ? View.VISIBLE : View.GONE);
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
                String.format(
                        viewHolder.itemView.getContext().getString(R.string.words_adapter_relations),
                        word.getRelations().stream().map(relation -> String.format(
                                viewHolder.itemView.getContext().getString(R.string.words_adapter_relation),
                                relation.getWord().getWord(),
                                relation.getRelation())).collect(Collectors.joining(",   "))),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        myViewHolder.updateOptionalViewsVisibility();
    }

    public Word getSelectedWord() {
        return vocabulary.getVocabulary().getWord(getSelectedIndex());
    }
}