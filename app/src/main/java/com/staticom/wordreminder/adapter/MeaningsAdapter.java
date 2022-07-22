package com.staticom.wordreminder.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.Word;

import java.util.stream.Collectors;

public class MeaningsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView meaning;
        private final TextView pronunciation;
        private final TextView example;
        private final TextView tags;

        public ViewHolder(View view) {
            super(view);

            meaning = view.findViewById(R.id.meaning);
            pronunciation = view.findViewById(R.id.pronunciation);
            example = view.findViewById(R.id.example);
            tags = view.findViewById(R.id.tags);

            onHolderDeactivated(true);
        }

        private void setOptionalViewsVisibility(boolean pronunciationVisibility, boolean exampleVisibility,
                                                boolean tagsVisibility) {
            pronunciation.setVisibility(pronunciationVisibility ? View.VISIBLE : View.GONE);
            example.setVisibility(exampleVisibility ? View.VISIBLE : View.GONE);
            tags.setVisibility(tagsVisibility ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onHolderActivated(boolean isBindMode) {
            final Meaning meaning = word.getMeaning(getAdapterPosition());

            setOptionalViewsVisibility(meaning.hasPronunciation(), meaning.hasExample(), !meaning.getTags().isEmpty());
        }

        @Override
        public void onHolderDeactivated(boolean isBindMode) {
            setOptionalViewsVisibility(false, false, false);
        }
    }

    private Word word;

    public MeaningsAdapter(Word word) {
        super(R.layout.item_meaning);

        this.word = word;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return word != null ? word.getMeanings().size() : 0;
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final ViewHolder myViewHolder = (ViewHolder)viewHolder;
        final Meaning meaning = word.getMeaning(position);

        myViewHolder.meaning.setText(meaning.getMeaning());
        myViewHolder.pronunciation.setText(HtmlCompat.fromHtml(
                String.format(
                        viewHolder.itemView.getContext().getString(R.string.meanings_adapter_pronunciation),
                        meaning.getPronunciation()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        myViewHolder.example.setText(HtmlCompat.fromHtml(
                String.format(
                        viewHolder.itemView.getContext().getString(R.string.meanings_adapter_example),
                        meaning.getExample()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        myViewHolder.example.setText(HtmlCompat.fromHtml(
                String.format(
                        viewHolder.itemView.getContext().getString(R.string.meanings_adapter_example),
                        meaning.getExample()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        myViewHolder.tags.setText(HtmlCompat.fromHtml(
                String.format(
                        viewHolder.itemView.getContext().getString(R.string.meanings_adapter_tags),
                        meaning.getTags().stream().map(Tag::getTag).collect(Collectors.joining(", "))),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (myViewHolder.itemView.isActivated()) {
            myViewHolder.onHolderActivated(true);
        } else {
            myViewHolder.onHolderDeactivated(true);
        }
    }

    public Meaning getSelectedMeaning() {
        return word.getMeaning(getSelectedIndex());
    }
}