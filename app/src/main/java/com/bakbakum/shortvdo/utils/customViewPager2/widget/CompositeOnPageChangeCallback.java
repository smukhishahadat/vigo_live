package com.bakbakum.shortvdo.utils.customViewPager2.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import com.bakbakum.shortvdo.utils.customViewPager2.widget.ViewPager2.OnPageChangeCallback;
import java.util.List;
/**
 * Dispatches {@link OnPageChangeCallback} events to subscribers.
 */
final class CompositeOnPageChangeCallback extends OnPageChangeCallback {
    @NonNull
    private final List<OnPageChangeCallback> mCallbacks;
    CompositeOnPageChangeCallback(int initialCapacity) {
        mCallbacks = new ArrayList<>(initialCapacity);
    }
    /**
     * Adds the given callback to the list of subscribers
     */
    void addOnPageChangeCallback(OnPageChangeCallback callback) {
        mCallbacks.add(callback);
    }
    /**
     * Removes the given callback from the list of subscribers
     */
    void removeOnPageChangeCallback(OnPageChangeCallback callback) {
        mCallbacks.remove(callback);
    }
    /**
     * @see OnPageChangeCallback#onPageScrolled(int, float, int)
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels) {
        try {
            for (OnPageChangeCallback callback : mCallbacks) {
                callback.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }
    /**
     * @see OnPageChangeCallback#onPageSelected(int)
     */
    @Override
    public void onPageSelected(int position) {
        try {
            for (OnPageChangeCallback callback : mCallbacks) {
                callback.onPageSelected(position);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }
    /**
     * @see OnPageChangeCallback#onPageScrollStateChanged(int)
     */
    @Override
    public void onPageScrollStateChanged(@ViewPager2.ScrollState int state) {
        try {
            for (OnPageChangeCallback callback : mCallbacks) {
                callback.onPageScrollStateChanged(state);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }
    private void throwCallbackListModifiedWhileInUse(ConcurrentModificationException parent) {
        throw new IllegalStateException(
                "Adding and removing callbacks during dispatch to callbacks is not supported",
                parent
        );
    }
}
