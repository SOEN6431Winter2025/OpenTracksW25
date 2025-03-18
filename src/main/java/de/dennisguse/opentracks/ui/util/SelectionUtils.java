package de.dennisguse.opentracks.ui.util;

import android.util.SparseBooleanArray;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling selection in RecyclerView adapters.
 */
public class SelectionUtils {

    /**
     * Gets an array of checked IDs from a selection array.
     * 
     * @param selection The SparseBooleanArray containing selection state
     * @return Array of selected IDs
     */
    public static long[] getCheckedIds(SparseBooleanArray selection) {
        List<Long> ids = new ArrayList<>();

        for (int i = 0; i < selection.size(); i++) {
            if (selection.valueAt(i)) {
                ids.add((long) selection.keyAt(i));
            }
        }

        return ids.stream().mapToLong(i -> i).toArray();
    }

    /**
     * Updates all visible child ViewHolders' selection state
     * 
     * @param recyclerView The RecyclerView containing ViewHolders
     * @param selection The SparseBooleanArray to clear if not selected
     * @param isSelected Whether to select or deselect
     */
    public static <T extends RecyclerView.ViewHolder> void updateSelectionStateForVisible(
            RecyclerView recyclerView,
            SparseBooleanArray selection,
            boolean isSelected,
            SelectionCallback<T> callback) {
        
        if (!isSelected) {
            selection.clear();
        }

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            T holder = (T) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            callback.setSelected(holder, isSelected);
        }
    }
    
    /**
     * Callback interface for selection operations
     */
    public interface SelectionCallback<T extends RecyclerView.ViewHolder> {
        void setSelected(T holder, boolean isSelected);
    }
}