package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.VocabularyList;
import com.staticom.wordreminder.core.VocabularyMetadata;

public class VocabularyListAdapter extends SelectableAdapter {

    public interface OnEditButtonClickListener {
        void onEditButtonClick(int index);
    }

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView name;
        private final TextView time;
        private final ImageButton edit;

        public ViewHolder(Context applicationContext, View view) {
            super(view);

            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
            edit = view.findViewById(R.id.edit);
        }

        @Override
        public void onHolderActivated(boolean requiredAnimation) {
            edit.setVisibility(View.VISIBLE);
            edit.setClickable(true);
        }

        @Override
        public void onHolderDeactivated(boolean requiredAnimation) {
            edit.setVisibility(View.GONE);
            edit.setClickable(false);
        }
    }

    private final VocabularyList vocabularyList;

    private OnEditButtonClickListener onEditButtonClickListener;

    public VocabularyListAdapter(VocabularyList vocabularyList) {
        super(R.layout.item_vocabulary);

        this.vocabularyList = vocabularyList;
    }

    @Override
    public int getItemCount() {
        return vocabularyList.getVocabularyList().size();
    }

    public void setOnEditButtonClickListener(OnEditButtonClickListener onEditButtonClickListener) {
        this.onEditButtonClickListener = onEditButtonClickListener;
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