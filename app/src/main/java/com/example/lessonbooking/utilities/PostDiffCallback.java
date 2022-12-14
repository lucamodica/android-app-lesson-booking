package com.example.lessonbooking.utilities;

import androidx.recyclerview.widget.DiffUtil;


import java.util.List;

public class PostDiffCallback<T> extends DiffUtil.Callback {

    private final List<T> oldPosts, newPosts;

    public PostDiffCallback(List<T> oldPosts, List<T> newPosts) {
        this.oldPosts = oldPosts;
        this.newPosts = newPosts;
    }
    /** All method for implementing the recycler view
     *
     * */

    @Override
    public int getOldListSize() {
        return oldPosts.size();
    }

    @Override
    public int getNewListSize() {
        return newPosts.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPosts.get(oldItemPosition).equals(newPosts.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPosts.get(oldItemPosition).equals(newPosts.get(newItemPosition));
    }
}
