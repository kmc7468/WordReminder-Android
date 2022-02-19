package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter extends RecyclerView.Adapter<SelectableAdapter.ViewHolder> {

    public interface OnItemSelectedListener {
        void onItemSelected(View view, int index);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(v -> {
                setSelectedIndex(getAdapterPosition());
            });
        }

        public void onHolderActivated(boolean requiredAnimation) {
        }

        public void onHolderDeactivated(boolean requiredAnimation) {
        }
    }

    private final int itemId;
    private final List<ViewHolder> viewHolders = new ArrayList<>();

    private int selectedIndex = -1;
    private OnItemSelectedListener onItemSelectedListener;

    public SelectableAdapter(int itemId) {
        this.itemId = itemId;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index != selectedIndex) {
            selectedIndex = index;
        } else return;

        ViewHolder selectedViewHolder = null;

        for (final ViewHolder viewHolder : viewHolders) {
            final int viewHolderIndex = viewHolder.getAdapterPosition();
            final boolean isViewHolderActivated = viewHolder.itemView.isActivated();

            if (viewHolderIndex == index) {
                selectedViewHolder = viewHolder;

                if (!isViewHolderActivated) {
                    viewHolder.itemView.setActivated(true);
                    viewHolder.onHolderActivated(true);
                }
            } else if (isViewHolderActivated) {
                viewHolder.itemView.setActivated(false);
                viewHolder.onHolderDeactivated(true);
            }
        }

        if (index != -1) {
            callOnItemSelected(selectedViewHolder);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    private void callOnItemSelected(ViewHolder viewHolder) {
        if (onItemSelectedListener != null) {
            onItemSelectedListener.onItemSelected(viewHolder.itemView, selectedIndex);
        }
    }

    protected abstract ViewHolder createViewHolder(View view);

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(itemId, parent, false);
        final ViewHolder viewHolder = createViewHolder(view);

        viewHolders.add(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (selectedIndex == position && !viewHolder.itemView.isActivated()) {
            viewHolder.itemView.setActivated(true);
            viewHolder.onHolderActivated(false);
        } else if (selectedIndex != position && viewHolder.itemView.isActivated()) {
            viewHolder.itemView.setActivated(false);
            viewHolder.onHolderDeactivated(false);
        }
    }
}