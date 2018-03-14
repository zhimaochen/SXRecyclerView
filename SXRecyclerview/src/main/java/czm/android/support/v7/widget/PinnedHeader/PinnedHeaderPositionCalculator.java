package czm.android.support.v7.widget.PinnedHeader;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import czm.android.support.v7.widget.LinearLayoutManager;
import czm.android.support.v7.widget.SXRecyclerView;
import czm.android.support.v7.widget.PinnedHeader.helper.DimensionCalculator;
import czm.android.support.v7.widget.PinnedHeader.helper.OrientationProvider;
import czm.android.support.v7.widget.PinnedHeader.helper.PinnedHeaderProvider;
import czm.android.support.v7.widget.RecyclerView;

/**
 * 计算 PinnedHeader View的位置
 */
public class PinnedHeaderPositionCalculator {

    private final RecyclerPinnedHeaderAdapter mHeaderAdapter;
    private final OrientationProvider mOrientationProvider;
    private final PinnedHeaderProvider mHeaderProvider;
    private final DimensionCalculator mDimensionCalculator;

    private final Rect mTempRect1 = new Rect();
    private final Rect mTempRect2 = new Rect();

    public PinnedHeaderPositionCalculator(RecyclerPinnedHeaderAdapter adapter, PinnedHeaderProvider headerProvider,
                                          OrientationProvider orientationProvider, DimensionCalculator dimensionCalculator) {
        mHeaderAdapter = adapter;
        mHeaderProvider = headerProvider;
        mOrientationProvider = orientationProvider;
        mDimensionCalculator = dimensionCalculator;
    }

    public boolean hasPinnedHeader(View itemView, int orientation, int position) {
        int offset, margin;
        mDimensionCalculator.initMargins(mTempRect1, itemView);
        if (orientation == LinearLayout.VERTICAL) {
            offset = itemView.getTop();
            margin = mTempRect1.top;
        } else {
            offset = itemView.getLeft();
            margin = mTempRect1.left;
        }

        return offset <= margin && mHeaderAdapter.getHeaderId(position) >= 0;
    }

    public boolean hasNewHeader(int position, boolean isReverseLayout) {
        if (indexOutOfBounds(position)) {
            return false;
        }

        long headerId = mHeaderAdapter.getHeaderId(position);

        if (headerId < 0) {
            return false;
        }

        long nextItemHeaderId = -1;
        int nextItemPosition = position + (isReverseLayout ? 1 : -1);
        if (!indexOutOfBounds(nextItemPosition)) {
            nextItemHeaderId = mHeaderAdapter.getHeaderId(nextItemPosition);
        }
        int firstItemPosition = isReverseLayout ? mHeaderAdapter.getItemCount() - 1 : 0;

        return position == firstItemPosition || headerId != nextItemHeaderId;
    }

    private boolean indexOutOfBounds(int position) {
        return position < 0 || position >= mHeaderAdapter.getItemCount();
    }

    /**
     * 初始化PinnedHeader的区域,并将结果设置到bounds中
     * @param bounds
     * @param recyclerView
     * @param header
     * @param firstView
     * @param firstHeader
     */
    public void initHeaderBounds(Rect bounds, SXRecyclerView recyclerView, View header, View firstView, boolean firstHeader) {
        int orientation = mOrientationProvider.getOrientation(recyclerView);
        initDefaultHeaderOffset(bounds, recyclerView, header, firstView, orientation); //根据位置初始化pinnedHeader的区域
        if (firstHeader && isStickyHeaderBeingPushedOffscreen(recyclerView, header)) {  //如果该header是第一个pinnedHeader并且处于被推出屏幕过程中,则进入该分支对该pinnedHeader的区域进行相应的偏移
            View viewAfterNextHeader = getFirstViewUnobscuredByHeader(recyclerView, header); //获取PinnedHeader下面的那个view
            int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterNextHeader);
            View secondHeader = mHeaderProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);  //第二个pinnedHeader
            translateHeaderWithNextHeader(recyclerView, mOrientationProvider.getOrientation(recyclerView), bounds,
                    header, viewAfterNextHeader, secondHeader); //对正在被推出屏幕的PinnedHeader计算需要进行的偏移,然后改变bounds数值
        }
    }

    private void initDefaultHeaderOffset(Rect headerMargins, RecyclerView recyclerView, View header, View firstView, int orientation) {
        int translationX, translationY;
        mDimensionCalculator.initMargins(mTempRect1, header);

        ViewGroup.LayoutParams layoutParams = firstView.getLayoutParams();
        int leftMargin = 0;
        int topMargin = 0;
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            leftMargin = marginLayoutParams.leftMargin;
            topMargin = marginLayoutParams.topMargin;
        }

        if (orientation == LinearLayoutManager.VERTICAL) {
            translationX = firstView.getLeft() - leftMargin + mTempRect1.left;
            translationY = Math.max(
                    firstView.getTop() - topMargin - header.getHeight() - mTempRect1.bottom,
                    getListTop(recyclerView) + mTempRect1.top);
        } else {
            translationY = firstView.getTop() - topMargin + mTempRect1.top;
            translationX = Math.max(
                    firstView.getLeft() - leftMargin - header.getWidth() - mTempRect1.right,
                    getListLeft(recyclerView) + mTempRect1.left);
        }

        headerMargins.set(translationX, translationY, translationX + header.getWidth(),
                translationY + header.getHeight());
    }

    /**
     * 该PinnedHeader是否正在处于被推出屏幕过程中
     * @param recyclerView
     * @param pinnedHeader
     * @return
     */
    private boolean isStickyHeaderBeingPushedOffscreen(SXRecyclerView recyclerView, View pinnedHeader) {
        View viewAfterHeader = getFirstViewUnobscuredByHeader(recyclerView, pinnedHeader);
        int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterHeader) - recyclerView.getHeaderViewsCount();
        if (firstViewUnderHeaderPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        boolean isReverseLayout = mOrientationProvider.isReverseLayout(recyclerView);
        if (firstViewUnderHeaderPosition > 0 && hasNewHeader(firstViewUnderHeaderPosition, isReverseLayout)) {
            View nextHeader = mHeaderProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);
            mDimensionCalculator.initMargins(mTempRect1, nextHeader);
            mDimensionCalculator.initMargins(mTempRect2, pinnedHeader);

            if (mOrientationProvider.getOrientation(recyclerView) == LinearLayoutManager.VERTICAL) {
                int topOfNextHeader = viewAfterHeader.getTop() - mTempRect1.bottom - nextHeader.getHeight() - mTempRect1.top;
                int bottomOfThisHeader = recyclerView.getPaddingTop() + pinnedHeader.getBottom() + mTempRect2.top + mTempRect2.bottom;
                if (topOfNextHeader < bottomOfThisHeader) {
                    return true;
                }
            } else {
                int leftOfNextHeader = viewAfterHeader.getLeft() - mTempRect1.right - nextHeader.getWidth() - mTempRect1.left;
                int rightOfThisHeader = recyclerView.getPaddingLeft() + pinnedHeader.getRight() + mTempRect2.left + mTempRect2.right;
                if (leftOfNextHeader < rightOfThisHeader) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *  对正在被推出屏幕的PinnedHeader计算需要进行偏移,放到translation中
     * @param recyclerView
     * @param orientation
     * @param translation
     * @param currentHeader
     * @param viewAfterNextHeader
     * @param nextHeader
     */
    private void translateHeaderWithNextHeader(SXRecyclerView recyclerView, int orientation, Rect translation,
                                               View currentHeader, View viewAfterNextHeader, View nextHeader) {
        mDimensionCalculator.initMargins(mTempRect1, nextHeader);
        mDimensionCalculator.initMargins(mTempRect2, currentHeader);
        if (orientation == LinearLayoutManager.VERTICAL) {
            int topOfStickyHeader = getListTop(recyclerView) + mTempRect2.top + mTempRect2.bottom;
            int shiftFromNextHeader = viewAfterNextHeader.getTop() - nextHeader.getHeight() - mTempRect1.bottom - mTempRect1.top - currentHeader.getHeight() - topOfStickyHeader;
            if (shiftFromNextHeader < topOfStickyHeader) {
                translation.top += shiftFromNextHeader;
                translation.bottom += shiftFromNextHeader;
            }
        } else {
            int leftOfStickyHeader = getListLeft(recyclerView) + mTempRect2.left + mTempRect2.right;
            int shiftFromNextHeader = viewAfterNextHeader.getLeft() - nextHeader.getWidth() - mTempRect1.right - mTempRect1.left - currentHeader.getWidth() - leftOfStickyHeader;
            if (shiftFromNextHeader < leftOfStickyHeader) {
                translation.left += shiftFromNextHeader;
                translation.right += shiftFromNextHeader;
            }
        }
    }

    /**
     * 获取第一个没有被Pinnedheader完全遮盖的itemview
     * @param parent
     * @param firstHeader
     * @return
     */
    private View getFirstViewUnobscuredByHeader(SXRecyclerView parent, View firstHeader) {
        boolean isReverseLayout = mOrientationProvider.isReverseLayout(parent);
        int step = isReverseLayout ? -1 : 1;
        int from = isReverseLayout ? parent.getChildCount() - 1 : 0;
        for (int i = from; i >= 0 && i <= parent.getChildCount() - 1; i += step) {
            View child = parent.getChildAt(i);
            if (!itemIsObscuredByHeader(parent, child, firstHeader, mOrientationProvider.getOrientation(parent))) {
                return child;
            }
        }
        return null;
    }

    /**
     * 判断该itemView是否被PinnedHeader完全遮盖住
     * @param parent
     * @param item
     * @param header
     * @param orientation
     * @return
     */
    private boolean itemIsObscuredByHeader(SXRecyclerView parent, View item, View header, int orientation) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) item.getLayoutParams();
        mDimensionCalculator.initMargins(mTempRect1, header);

        int adapterPosition = parent.getChildAdapterPosition(item) - parent.getHeaderViewsCount();
        if (adapterPosition == RecyclerView.NO_POSITION || mHeaderProvider.getHeader(parent, adapterPosition) != header) {
            return false;
        }

        if (orientation == LinearLayoutManager.VERTICAL) {
            int itemTop = item.getTop() - layoutParams.topMargin;
            int headerBottom = header.getBottom() + mTempRect1.bottom + mTempRect1.top;
            if (itemTop > headerBottom) {
                return false;
            }
        } else {
            int itemLeft = item.getLeft() - layoutParams.leftMargin;
            int headerRight = header.getRight() + mTempRect1.right + mTempRect1.left;
            if (itemLeft > headerRight) {
                return false;
            }
        }

        return true;
    }

    private int getListTop(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingTop();
        } else {
            return 0;
        }
    }

    private int getListLeft(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingLeft();
        } else {
            return 0;
        }
    }
}
