package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.VocabularyList;
import com.staticom.wordreminder.core.VocabularyMetadata;

public class VocabularyListAdapter extends SelectableAdapter {

    public interface OnOpenButtonClickListener {
        void onOpenButtonClick(int index);
    }

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView name;
        private final TextView time;
        private final ImageButton open;

        private final Animation openOpenAnimation, openCloseAnimation;

        public ViewHolder(Context applicationContext, View view) {
            super(view);

            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
            open = view.findViewById(R.id.open);

            open.setOnClickListener(v -> {
                if (onOpenButtonClickListener != null) {
                    onOpenButtonClickListener.onOpenButtonClick(getSelectedIndex());
                }
            });

            openOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open);
            openCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close);
        }

        @Override
        public void onHolderActivated(boolean requiredAnimation) {
            if (requiredAnimation) {
                open.startAnimation(openOpenAnimation);
            }

            open.setVisibility(View.VISIBLE);
            open.setClickable(true);
        }

        @Override
        public void onHolderDeactivated(boolean requiredAnimation) {
            if (requiredAnimation) {
                open.startAnimation(openCloseAnimation);
            }

            open.setVisibility(View.INVISIBLE);
            open.setClickable(false);
        }
    }

    private final VocabularyList vocabularyList;

    private OnOpenButtonClickListener onOpenButtonClickListener;

    public VocabularyListAdapter(VocabularyList vocabularyList) {
        super(R.layout.item_vocabulary);

        this.vocabularyList = vocabularyList;
    }

    @Override
    public int getItemCount() {
        return vocabularyList.getVocabularyList().size();
    }

    public void setOnOpenButtonClickListener(OnOpenButtonClickListener onOpenButtonClickListener) {
        this.onOpenButtonClickListener = onOpenButtonClickListener;
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view.getContext().getApplicationContext(), view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final ViewHolder myViewHolder = (ViewHolder)viewHolder;
        final VocabularyMetadata vocabulary = vocabularyList.getVocabulary(position);

        myViewHolder.name.setText(vocabulary.getName());
        myViewHolder.time.setText(vocabulary.getTime().toString());
    }
}