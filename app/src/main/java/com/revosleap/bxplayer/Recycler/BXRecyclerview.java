

package com.revosleap.bxplayer.Recycler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

/**
 * Subclass of the standard recyclerview that adds a convenience method for a item clicklistener
 * and adds the option to handle a proper context menu.
 */
public class BXRecyclerview extends RecyclerView {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private RecyclerViewContextMenuInfo mContextMenuInfo;

    public BXRecyclerview(Context context) {
        super(context);
    }

    public BXRecyclerview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BXRecyclerview(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getChildLayoutPosition(originalView);
        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            mContextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public void addOnItemClicklistener(final OnItemClickListener onItemClickListener) {
        addOnItemTouchListener(new RecyclerViewOnItemClickListener(getContext(), onItemClickListener));
    }

    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

        final public int position;

        final public long id;

        RecyclerViewContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }
    }

    private class RecyclerViewOnItemClickListener implements RecyclerView.OnItemTouchListener {

        private final OnItemClickListener mOnItemClickListener;

        private final GestureDetector mGestureDetector;

        RecyclerViewOnItemClickListener(Context context, @NonNull OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent motionEvent) {
            final View childView = view.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
            if (childView != null && mGestureDetector.onTouchEvent(motionEvent)) {
                childView.playSoundEffect(SoundEffectConstants.CLICK);
                mOnItemClickListener.onItemClick(view.getChildAdapterPosition(childView));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
