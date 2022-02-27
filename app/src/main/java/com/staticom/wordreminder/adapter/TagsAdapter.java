package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.Vocabulary;

public class TagsAdapter extends SelectableAdapter {

    public interface OnListButtonClickListener {
        void onListButtonClick(int index);
    }

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView tag;
        private final TextView count;
        private final ImageButton list;

        private final Animation listOpenAnimation, listCloseAnimation;

        public ViewHolder(Context applicationContext, View view) {
            super(view);

            tag = view.findViewById(R.id.tag);
            count = view.findViewById(R.id.count);
            list = view.findViewById(R.id.list);

            list.setOnClickListener(v -> {
                if (onListButtonClickListener != null) {
                    onListButtonClickListener.onListButtonClick(getSelectedIndex());
                }
            });

            listOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open);
            listCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close);
        }

        @Override
        public void onHolderActivated(boolean requiredAnimation) {
            if (requiredAnimation) {
                list.startAnimation(listOpenAnimation);
            }

            list.setVisibility(View.VISIBLE);
            list.setClickable(true);
        }

        @Override
        public void onHolderDeactivated(boolean requiredAnimation) {
            if (requiredAnimation) {
                list.startAnimation(listCloseAnimation);
            }

            list.setVisibility(View.INVISIBLE);
            list.setClickable(false);
        }
    }

    private final Vocabulary vocabulary;

    private OnListButtonClickListener onListButtonClickListener;

    public TagsAdapter(Vocabulary vocabulary) {
        super(R.layout.item_tag);

        this.vocabulary = vocabulary;
    }

    public void setOnListButtonClickListener(OnListButtonClickListener onListButtonClickListener) {
        this.onListButtonClickListener = onListButtonClickListener;
    }

    @Override
    public int getItemCount() {
        return vocabulary.getTags().size();
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view.getContext().getApplicationContext(), view);
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
