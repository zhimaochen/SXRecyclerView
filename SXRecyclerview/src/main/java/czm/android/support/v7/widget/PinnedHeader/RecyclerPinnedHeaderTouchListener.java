package czm.android.support.v7.widget.PinnedHeader;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import czm.android.support.v7.widget.HeaderAndFooterWrapperAdapter;
import czm.android.support.v7.widget.SXRecyclerView;
import czm.android.support.v7.widget.RecyclerView;

public class RecyclerPinnedHeaderTouchListener implements SXRecyclerView.OnItemTouchListener {
    private final GestureDetector mTapDetector;
    private final SXRecyclerView mRecyclerView;
    private final RecyclerPinnedHeaderDecoration mDecor;
    private OnHeaderClickListener mOnHeaderClickListener;

    public interface OnHeaderClickListener {
        void onHeaderClick(View header, int position, long headerId, MotionEvent e);
    }

    public RecyclerPinnedHeaderTouchListener(final SXRecyclerView recyclerView,
                                             final RecyclerPinnedHeaderDecoration decor) {
        mTapDetector = new GestureDetector(recyclerView.getContext(), new SingleTapDetector());
        mRecyclerView = recyclerView;
        mDecor = decor;
    }

    public RecyclerPinnedHeaderAdapter getAdapter() {
        if (mRecyclerView.getAdapter() instanceof HeaderAndFooterWrapperAdapter && ((HeaderAndFooterWrapperAdapter)mRecyclerView.getAdapter()).getWrappedAdapter() instanceof RecyclerPinnedHeaderAdapter){
            return (RecyclerPinnedHeaderAdapter)((HeaderAndFooterWrapperAdapter)mRecyclerView.getAdapter()).getWrappedAdapter();
        }else if (mRecyclerView.getAdapter() instanceof RecyclerPinnedHeaderAdapter) {
            return (RecyclerPinnedHeaderAdapter) mRecyclerView.getAdapter();
        } else {
            String ex = "SXRecyclerView with " +
                    RecyclerPinnedHeaderTouchListener.class.getSimpleName() +
                    " requires a " + RecyclerPinnedHeaderAdapter.class.getSimpleName();
            throw new IllegalStateException(ex);
        }
    }


    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        mOnHeaderClickListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        if (this.mOnHeaderClickListener != null) {
            boolean tapDetectorResponse = this.mTapDetector.onTouchEvent(e);
            if (tapDetectorResponse) {
                // Don't return false if a single tap is detected
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                int position = mDecor.findHeaderPositionUnder((int) e.getX(), (int) e.getY());
                return position != -1;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent e) { /* do nothing? */ }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // do nothing
    }

    private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int position = mDecor.findHeaderPositionUnder((int) e.getX(), (int) e.getY());
            if (position != -1) {
                View headerView = mDecor.getHeaderView(mRecyclerView, position);
                long headerId = getAdapter().getHeaderId(position);
                mOnHeaderClickListener.onHeaderClick(headerView, position, headerId, e);
                mRecyclerView.playSoundEffect(SoundEffectConstants.CLICK);
                headerView.onTouchEvent(e);
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    }
}
