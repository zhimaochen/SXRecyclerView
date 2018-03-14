package czm.android.support.v7.widget.PinnedHeader;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;

import czm.android.support.v7.widget.LinearLayoutManager;
import czm.android.support.v7.widget.SXRecyclerView;
import czm.android.support.v7.widget.PinnedHeader.helper.DimensionCalculator;
import czm.android.support.v7.widget.PinnedHeader.helper.LinearLayoutOrientationProvider;
import czm.android.support.v7.widget.PinnedHeader.helper.OrientationProvider;
import czm.android.support.v7.widget.PinnedHeader.helper.PinnedHeaderProvider;
import czm.android.support.v7.widget.PinnedHeader.helper.PinnedHeaderRenderer;
import czm.android.support.v7.widget.PinnedHeader.helper.PinnedHeaderViewCache;
import czm.android.support.v7.widget.RecyclerView;

public class RecyclerPinnedHeaderDecoration extends SXRecyclerView.ItemDecoration {

    private final RecyclerPinnedHeaderAdapter mHeaderAdapter;
    private final SparseArray<Rect> mHeaderRects = new SparseArray<>();
    private final PinnedHeaderProvider mHeaderProvider;
    private final OrientationProvider mOrientationProvider;
    private final PinnedHeaderPositionCalculator mHeaderPositionCalculator;
    private final PinnedHeaderRenderer mRenderer;
    private final DimensionCalculator mDimensionCalculator;

    private final Rect mTempRect = new Rect();

    private SXRecyclerView mRecyclerView;

    //这几个变量用于在顶部PinnedHeader改变时触发回调
    private long mCurrentHeaderId,mLastHeaderId = Integer.MIN_VALUE;
    private int mCurrentPosition,mLastPosition = Integer.MIN_VALUE;
    private View mLastHeader;

    private OnPinnedHeaderChangeListener mOnPinnedHeaderChangeListener;

    public RecyclerPinnedHeaderDecoration(RecyclerPinnedHeaderAdapter adapter) {
        this(adapter, new LinearLayoutOrientationProvider(), new DimensionCalculator());
    }

    private RecyclerPinnedHeaderDecoration(RecyclerPinnedHeaderAdapter adapter, OrientationProvider orientationProvider,
                                           DimensionCalculator dimensionCalculator) {
        this(adapter, orientationProvider, dimensionCalculator, new PinnedHeaderRenderer(orientationProvider),
                new PinnedHeaderViewCache(adapter, orientationProvider));
    }

    private RecyclerPinnedHeaderDecoration(RecyclerPinnedHeaderAdapter adapter, OrientationProvider orientationProvider,
                                           DimensionCalculator dimensionCalculator, PinnedHeaderRenderer headerRenderer, PinnedHeaderProvider headerProvider) {
        this(adapter, headerRenderer, orientationProvider, dimensionCalculator, headerProvider,
                new PinnedHeaderPositionCalculator(adapter, headerProvider, orientationProvider,
                        dimensionCalculator));
    }

    private RecyclerPinnedHeaderDecoration(RecyclerPinnedHeaderAdapter adapter, PinnedHeaderRenderer headerRenderer,
                                           OrientationProvider orientationProvider, DimensionCalculator dimensionCalculator, PinnedHeaderProvider headerProvider,
                                           PinnedHeaderPositionCalculator headerPositionCalculator) {
        mHeaderAdapter = adapter;
        mHeaderProvider = headerProvider;
        mOrientationProvider = orientationProvider;
        mRenderer = headerRenderer;
        mDimensionCalculator = dimensionCalculator;
        mHeaderPositionCalculator = headerPositionCalculator;
        mLastPosition = 0;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView recyclerView, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, recyclerView, state);

        checkIfSXRecyclerView(recyclerView);
        SXRecyclerView parent = (SXRecyclerView) recyclerView;

        int itemPosition = parent.getChildAdapterPosition(view) - parent.getHeaderViewsCount();
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (mHeaderPositionCalculator.hasNewHeader(itemPosition, mOrientationProvider.isReverseLayout(parent))) {
            View header = getHeaderView(parent, itemPosition);
            setItemOffsetsForHeader(outRect, header, mOrientationProvider.getOrientation(parent));
        }
    }

    private void setItemOffsetsForHeader(Rect itemOffsets, View header, int orientation) {
        mDimensionCalculator.initMargins(mTempRect, header);
        if (orientation == LinearLayoutManager.VERTICAL) {
            itemOffsets.top = header.getHeight() + mTempRect.top + mTempRect.bottom;
        } else {
            itemOffsets.left = header.getWidth() + mTempRect.left + mTempRect.right;
        }
    }

    @Override
    public void onDrawUnderForeground(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        super.onDrawUnderForeground(canvas, recyclerView, state);
        mRecyclerView = (SXRecyclerView) recyclerView;
        checkIfSXRecyclerView(recyclerView);
        SXRecyclerView parent = (SXRecyclerView) recyclerView;

        final int childCount = parent.getChildCount();
        if (childCount <= 0 || mHeaderAdapter.getItemCount() <= 0) {
            return;
        }

        for (int i = 0; i < childCount; i++) {
            View itemView = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(itemView) - mRecyclerView.getHeaderViewsCount();
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            boolean hasPinnedHeader = mHeaderPositionCalculator.hasPinnedHeader(itemView, mOrientationProvider.getOrientation(parent), position);
            if (hasPinnedHeader || mHeaderPositionCalculator.hasNewHeader(position, mOrientationProvider.isReverseLayout(parent))) {

                View header = mHeaderProvider.getHeader(parent, position);
                if (mOnPinnedHeaderChangeListener != null){
                    mCurrentPosition = getLinearLayoutManager().findFirstVisibleItemPosition();
                    mCurrentHeaderId = getAdapter().getHeaderId(mCurrentPosition);
                    mLastHeaderId = getAdapter().getHeaderId(mLastPosition);
                    mLastHeader = mHeaderProvider.getHeader(parent, mLastPosition);
                    if (mCurrentHeaderId != mLastHeaderId){
                        mOnPinnedHeaderChangeListener.OnPinnedHeaderChange(parent,header,mCurrentPosition,mCurrentHeaderId,mLastHeader,mLastPosition,mLastHeaderId);
                        mLastHeaderId = mCurrentHeaderId;
                        mLastPosition = mCurrentPosition;
                    }
                }
                //re-use existing Rect, if any.
                Rect headerOffset = mHeaderRects.get(position);
                if (headerOffset == null) {
                    headerOffset = new Rect();
                    mHeaderRects.put(position, headerOffset);
                }

                mHeaderPositionCalculator.initHeaderBounds(headerOffset, parent, header, itemView, hasPinnedHeader);
                mRenderer.drawHeader(parent, canvas, header, headerOffset);

            }
        }
    }

    public int findHeaderPositionUnder(int x, int y) {
        int firstVisibleItemPosition = 0;
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() instanceof LinearLayoutManager){
            firstVisibleItemPosition = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            if (firstVisibleItemPosition < 0){
                return -1;
            }
        }
        for (int i = firstVisibleItemPosition; i < mHeaderRects.size(); i++) {
            Rect rect = mHeaderRects.get(mHeaderRects.keyAt(i));
            if (rect.contains(x, y)) {
                return mHeaderRects.keyAt(i);
            }
        }
        return -1;
    }

    private void checkIfSXRecyclerView(RecyclerView recyclerView) {
        if (!(recyclerView instanceof SXRecyclerView)) {
            String ex = RecyclerPinnedHeaderDecoration.class.getSimpleName()
                    + " only surport SXRecyclerView.";
            throw new IllegalStateException(ex);
        }
    }

    public View getHeaderView(SXRecyclerView parent, int position) {
        return mHeaderProvider.getHeader(parent, position);
    }

    /**
     * 用于清除PinneadHeader缓存
     */
    public void invalidateHeaders() {
        mHeaderProvider.invalidate();
    }

    /**
     * 得到对应的adapter
     * @return
     */
    public RecyclerPinnedHeaderAdapter getAdapter() {
        if (mRecyclerView.getAdapter() instanceof RecyclerPinnedHeaderAdapter) {
            return (RecyclerPinnedHeaderAdapter) mRecyclerView.getAdapter();
        } else {
            String ex = "SXRecyclerView with " +
                    RecyclerPinnedHeaderDecoration.class.getSimpleName() +
                    " requires a " + RecyclerPinnedHeaderAdapter.class.getSimpleName();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 得到对应的LayoutManager
     * @return
     */
    public LinearLayoutManager getLinearLayoutManager() {
        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            return (LinearLayoutManager) mRecyclerView.getLayoutManager();
        } else {
            String ex = "SXRecyclerView with " +
                    RecyclerPinnedHeaderDecoration.class.getSimpleName() +
                    " requires a " + LinearLayoutManager.class.getSimpleName();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 顶部PinnedHeader改变时的回调接口
     */
    public interface OnPinnedHeaderChangeListener{
        /**
         *
         * @param recyclerView
         * @param currentHeader  现在的PinnedHeader
         * @param currentPosition  现在的PinnedHeader对应的item的position
         * @param currentHeaderId  现在的PinnedHeader的Id
         * @param lastHeader  改变之前的PinnedHeader
         * @param lastPosition  改变之前的PinnedHeader对应的item的position
         * @param lastHeaderId 改变之前的PinnedHeader的Id
         */
        void OnPinnedHeaderChange(RecyclerView recyclerView, View currentHeader, int currentPosition, long currentHeaderId, View lastHeader, int lastPosition, long lastHeaderId);
    }

    /**
     * 设置PinnedHeader改变监听器
     * @param listener
     */
    public void setPinnedHeaderListener(OnPinnedHeaderChangeListener listener){
        mOnPinnedHeaderChangeListener = listener;
    }
}
