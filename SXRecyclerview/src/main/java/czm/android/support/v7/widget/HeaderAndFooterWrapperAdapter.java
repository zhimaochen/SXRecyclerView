package czm.android.support.v7.widget;

import android.support.v4.util.SparseArrayCompat;
import android.view.ViewGroup;

import java.util.List;

import czm.android.support.v7.widget.SXRecyclerView.FixedViewInfo;
import czm.android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * 采用装饰者模式，让RecyclerView支持添加多个Header 或者Footer view，
 */
public class HeaderAndFooterWrapperAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "HeaderAndFooterWrapperAdapter";

    private static final int BASE_ITEM_TYPE_HEADER = 100000;
    private static final int BASE_ITEM_TYPE_FOOTER = 200000;

    private SparseArrayCompat<FixedViewInfo> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<FixedViewInfo> mFooterViews = new SparseArrayCompat<>();

    private RecyclerView.Adapter mInnerAdapter;
    private static int mHeadIndex = BASE_ITEM_TYPE_HEADER;
    private static int mFooterIndex = BASE_ITEM_TYPE_FOOTER;

    public HeaderAndFooterWrapperAdapter(RecyclerView.Adapter adapter) {
        mInnerAdapter = adapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            RecyclerView.ViewHolder holder = mHeaderViews.get(viewType).viewHolder;
            return holder;

        } else if (mFooterViews.get(viewType) != null) {
            RecyclerView.ViewHolder holder = mFooterViews.get(viewType).viewHolder;
            return holder;
        }
        if (mInnerAdapter != null) {
            return mInnerAdapter.onCreateViewHolder(parent, viewType);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterViewPos(position)) {
            return mFooterViews.keyAt(position - getHeadersCount() - getRealItemCount());
        }
        if (mInnerAdapter != null) {
            return mInnerAdapter.getItemViewType(position - getHeadersCount());
        }
        return -2;
    }

    private int getRealItemCount() {
        if (mInnerAdapter != null) {
            return mInnerAdapter.getItemCount();
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderViewPos(position)) {
            return;
        }
        if (isFooterViewPos(position)) {
            return;
        }
        if (mInnerAdapter != null) {
            mInnerAdapter.onBindViewHolder(holder, position - getHeadersCount());
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (isHeaderViewPos(position)) {
            return;
        }
        if (isFooterViewPos(position)) {
            return;
        }
        if (mInnerAdapter != null) {
            mInnerAdapter.onBindViewHolder(holder, position - getHeadersCount(), payloads);
        }
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + getRealItemCount();
    }

    /**
     * 在使用了GridLayoutManager的情况下，不让header或者footer view当做普通的item来对待，需要重写此方法进行相关处理
     * 当发现layoutManager为GridLayoutManager时，通过设置SpanSizeLookup，对其getSpanSize方法，返回值设置为layoutManager.getSpanCount()
     *
     * @param recyclerView The RecyclerView instance which started observing this adapter.
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (mInnerAdapter != null) {
            mInnerAdapter.onAttachedToRecyclerView(recyclerView);
        }
        setHeaderAndFooterSpanForGridLayoutManager(recyclerView);
    }


    /**
     * 如果是GridLayoutManager,则需要通过该方法设置header和footer的宽度
     *
     * @param recyclerView
     */
    public void setHeaderAndFooterSpanForGridLayoutManager(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = getItemViewType(position);
                    if (mHeaderViews.get(viewType) != null) {
                        return gridLayoutManager.getSpanCount();
                    } else if (mFooterViews.get(viewType) != null) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (mInnerAdapter != null) {
            mInnerAdapter.onDetachedFromRecyclerView(recyclerView);
        }
    }

    /**
     * 在使用了StaggeredGridLayoutManager的情况下，不让header或者footer view当做普通的item来对待，需要重写此方法进行相关处理
     * 当发现layoutManager为GridLayoutManager时，通过调用setFullSpan()
     *
     * @param holder Holder of the view being attached
     */
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (mInnerAdapter != null) {
            mInnerAdapter.onViewAttachedToWindow(holder);
        }
        int position = holder.getLayoutPosition();
        if (isHeaderViewPos(position) || isFooterViewPos(position)) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        if (mInnerAdapter != null) {
            mInnerAdapter.onViewDetachedFromWindow(holder);
        }
    }

    private boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    private boolean isFooterViewPos(int position) {
        if (position >= getItemCount()) {
            return false;
        }
        return position >= getHeadersCount() + getRealItemCount();
    }

    public void addHeaderView(FixedViewInfo viewInfo) {
        mHeaderViews.put(mHeadIndex++, viewInfo);
    }

    public void addFooterView(FixedViewInfo viewInfo) {
        mFooterViews.put(mFooterIndex++, viewInfo);
    }

    public boolean removeHeaderView(FixedViewInfo viewInfo) {
        if (viewInfo == null) {
            return false;
        }

        if (mHeaderViews.size() > 0) {
            for (int i = 0; i < mHeaderViews.size(); i++) {
                FixedViewInfo temp = mHeaderViews.valueAt(i);
                if (viewInfo == temp) {
                    mHeaderViews.removeAt(i);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeFooterView(FixedViewInfo viewInfo) {
        if (viewInfo == null) {
            return false;
        }

        if (mFooterViews.size() > 0) {
            for (int i = 0; i < mFooterViews.size(); i++) {
                FixedViewInfo temp = mFooterViews.valueAt(i);
                if (viewInfo == temp) {
                    mFooterViews.removeAt(i);
                    return true;
                }
            }
        }
        return false;
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFootersCount() {
        return mFooterViews.size();
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mInnerAdapter.registerAdapterDataObserver(observer);
        super.registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mInnerAdapter.unregisterAdapterDataObserver(observer);
        super.unregisterAdapterDataObserver(mDataObserver);
    }


    @Override
    public boolean isEnable(int position) {
        int numHeaders = getHeadersCount();
        if (position >= 0 && position < numHeaders) {
            FixedViewInfo viewInfo = mHeaderViews.valueAt(position);
            if (viewInfo != null) {
                return viewInfo.isClickable;
            } else {
                return false;
            }
        }
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mInnerAdapter != null && position >= numHeaders) {
            adapterCount = getRealItemCount();
            if (adjPosition < adapterCount) {
                return mInnerAdapter.isEnable(adjPosition);
            }
        }
        int footerPosition = adjPosition - adapterCount;
        if (footerPosition >= 0 && footerPosition < getFootersCount()) {
            FixedViewInfo viewInfo = mFooterViews.valueAt(footerPosition);
            if (viewInfo != null) {
                return viewInfo.isClickable;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isSelectable(int position) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return false;
        }
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mInnerAdapter != null && position >= numHeaders) {
            adapterCount = getRealItemCount();
            if (adjPosition < adapterCount) {
                return mInnerAdapter.isSelectable(adjPosition);
            }
        }
        return false;
    }

    @Override
    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (mInnerAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = getRealItemCount();
            if (adjPosition < adapterCount) {
                return mInnerAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        if (mInnerAdapter != null) {
            mInnerAdapter.onViewRecycled(holder);
        }
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolder holder) {
        if (mInnerAdapter != null) {
            return mInnerAdapter.onFailedToRecycleView(holder);
        }
        return super.onFailedToRecycleView(holder);
    }

    public RecyclerView.Adapter getWrappedAdapter() {
        return mInnerAdapter;
    }

    @Override
    public boolean hasStableIds() {
        if (mInnerAdapter != null) {
            return mInnerAdapter.hasStableIds();
        }
        return super.hasStableIds();
    }

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onChanged() {
            if (mInnerAdapter != null) {
                mInnerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mInnerAdapter != null) {
                mInnerAdapter.notifyItemRangeInserted(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (mInnerAdapter != null) {
                mInnerAdapter.notifyItemRangeChanged(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (mInnerAdapter != null) {
                mInnerAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (mInnerAdapter != null) {
                mInnerAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (mInnerAdapter != null) {
                mInnerAdapter.notifyItemMoved(fromPosition, toPosition);
            }
        }
    };

}
