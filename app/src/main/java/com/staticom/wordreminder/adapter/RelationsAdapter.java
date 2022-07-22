package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Relation;
import com.staticom.wordreminder.core.Word;

public class RelationsAdapter extends SelectableAdapter {

    public interface OnSearchButtonClickListener {
        void onSearchButtonClick(int index);
    }

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView word;
        private final TextView relation;
        private final ImageButton select;

        private final Animation selectOpenAnimation, selectCloseAnimation;

        public ViewHolder(Context applicationContext, View view) {
            super(view);

            word = view.findViewById(R.id.word);
            relation = view.findViewById(R.id.relation);
            select = view.findViewById(R.id.select);

            select.setOnClickListener(v -> {
                if (onSearchButtonClickListener != null) {
                    onSearchButtonClickListener.onSearchButtonClick(getSelectedIndex());
                }
            });

            selectOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open);
            selectCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close);
        }

        @Override
        public void onHolderActivated(boolean requiredAnimation) {
            if (requiredAnimation) {
                select.startAnimation(selectOpenAnimation);
            }

            select.setVisibility(View.VISIBLE);
            select.setClickable(true);
        }

        @Override
        public void onHolderDeactivated(boolean requiredAnimation) {
            if (requiredAnimation) {
                select.startAnimation(selectCloseAnimation);
            }

            select.setVisibility(View.INVISIBLE);
            select.setClickable(false);
        }
    }

    private final Word word;

    private OnSearchButtonClickListener onSearchButtonClickListener;

    public RelationsAdapter(Word word) {
        super(R.layout.item_relation);

        this.word = word;
    }

    public void setOnSearchButtonClickListener(OnSearchButtonClickListener onSearchButtonClickListener) {
        this.onSearchButtonClickListener = onSearchButtonClickListener;
    }

    @Override
    public int getItemCount() {
        return word.getRelations().size();
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new RelationsAdapter.ViewHolder(view.getContext().getApplicationContext(), view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final RelationsAdapter.ViewHolder myViewHolder = (RelationsAdapter.ViewHolder)viewHolder;
        final Relation relation = word.getRelation(position);

        myViewHolder.word.setText(relation.getWord().getWord());
        myViewHolder.relation.setText(relation.getRelation());
    }
}
