package com.staticom.wordreminder.adapter;

import android.view.View;
import android.widget.TextView;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.Vocabulary;

public class TagsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView tag;
        private final TextView count;

        public ViewHolder(View view) {
            super(view);

            tag = view.findViewById(R.id.tag);
            count = view.findViewById(R.id.count);
        }
    }

    private final Vocabulary vocabulary;

    public TagsAdapter(Vocabulary vocabulary) {
        super(R.layout.item_tag);

        this.vocabulary = vocabulary;
    }

    @Override
    public int getItemCount() {
        return vocabulary.getTags().size();
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final ViewHolder myViewHolder = (ViewHolder)viewHolder;
        final Tag tag = vocabulary.getTag(position);

        myViewHolder.tag.setText(tag.getTag());
        myViewHolder.count.setText(String.format(
                viewHolder.itemView.getContext().getString(R.string.tags_adapter_count),
                tag.getWords().size(), tag.getMeanings().size()));
    }
}
