package com.staticom.wordreminder.utility;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.R;

public class RecyclerViewEmptyObserver extends RecyclerView.AdapterDataObserver {

    private final RecyclerView recyclerView;
    private final TextView emptyTextView;

    private final Animation tvOpen, tvClose;

    public RecyclerViewEmptyObserver(RecyclerView recyclerView, TextView emptyTextView) {
        this.recyclerView = recyclerView;
        this.emptyTextView = emptyTextView;

        tvOpen = AnimationUtils.loadAnimation(recyclerView.getContext().getApplicationContext(), R.anim.tv_open);
        tvClose = AnimationUtils.loadAnimation(recyclerView.getContext().getApplicationContext(), R.anim.tv_close);

        checkIsEmpty();
    }

    private void checkIsEmpty() {
        final boolean isEmpty = recyclerView.getAdapter().getItemCount() == 0;

        if (isEmpty) {
            emptyTextView.startAnimation(tvOpen);
        } else {
            emptyTextView.startAnimation(tvClose);
        }

        recyclerView.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onChanged() {
        checkIsEmpty();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        checkIsEmpty();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        checkIsEmpty();
    }
}