package czm.android.support.v7.widget.PinnedHeader.helper;

import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;

import czm.android.support.v7.widget.LinearLayoutManager;
import czm.android.support.v7.widget.SXRecyclerView;
import czm.android.support.v7.widget.PinnedHeader.RecyclerPinnedHeaderAdapter;

public class PinnedHeaderViewCache implements PinnedHeaderProvider {

    private final RecyclerPinnedHeaderAdapter mHeaderAdapter;
    private final LongSparseArray<View> mHeaderViews = new LongSparseArray<>();
    private final OrientationProvider mOrientationProvider;

    public PinnedHeaderViewCache(RecyclerPinnedHeaderAdapter adapter,
                                 OrientationProvider orientationProvider) {
        mHeaderAdapter = adapter;
        mOrientationProvider = orientationProvider;
    }

    @Override
    public View getHeader(SXRecyclerView parent, int position) {
        long headerId = mHeaderAdapter.getHeaderId(position);

        View header = mHeaderViews.get(headerId);
        if (header == null) {
            SXRecyclerView.ViewHolder viewHolder = mHeaderAdapter.onCreateHeaderViewHolder(parent);
            mHeaderAdapter.onBindHeaderViewHolder(viewHolder, position);
            header = viewHolder.itemView;
            if (header.getLayoutParams() == null) {
                header.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            int widthSpec;
            int heightSpec;

            if (mOrientationProvider.getOrientation(parent) == LinearLayoutManager.VERTICAL) {
                widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
                heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);
            } else {
                widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED);
                heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY);
            }

            int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                    parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
            int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                    parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);
            header.measure(childWidth, childHeight);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
            //过滤掉未测量成功的HeaderView
            if (parent.getHeight() != 0 || parent.getWidth() != 0){
                mHeaderViews.put(headerId, header);
            }
        }
        return header;
    }

    @Override
    public void invalidate() {
        mHeaderViews.clear();
    }
}
