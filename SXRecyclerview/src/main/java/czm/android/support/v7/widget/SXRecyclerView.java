package czm.android.support.v7.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.StateSet;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;

import java.util.ArrayList;
import java.util.List;

import czm.android.support.v7.R;
import czm.android.support.v7.widget.PinnedHeader.RecyclerPinnedHeaderDecoration;


/**
 * Created by CZM on 2018/1/20.
 */
public class SXRecyclerView extends RecyclerView {
    private static final String TAG = "SXRecyclerView";

    private boolean DEBUG = false;

    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    public static final int INVALID_POSITION = NO_POSITION;
    private static final int INVALID_POINTER = -1;

    static final int TOUCH_MODE_REST = -1;
    static final int TOUCH_MODE_DOWN = 0;
    static final int TOUCH_MODE_DONE_WAITING = 2;
    static final int TOUCH_MODE_SCROLL = 3;
    static final int TOUCH_MODE_FLING = 4;
    int mTouchMode = TOUCH_MODE_REST;

    Drawable mSelector;
    Rect mSelectorRect = new Rect();
    int mSelectorPosition = INVALID_POSITION;
    int mSelectionLeftPadding = 0;
    int mSelectionTopPadding = 0;
    int mSelectionRightPadding = 0;
    int mSelectionBottomPadding = 0;

    boolean mIsSdkVersion21 = false;


    private ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList<>();
    private ArrayList<FixedViewInfo> mFooterViewInfos = new ArrayList<>();

    /**
     * 选择模式，代表不进行选择操作
     */
    public static final int CHOICE_MODE_NONE = 0;
    /**
     * 选择模式，代表多选操作
     */
    public static final int CHOICE_MODE_MULTIPLE = 1;

    /**
     * 默认选择模式为CHOICE_MODE_NONE
     */
    int mChoiceMode = CHOICE_MODE_NONE;


    ActionMode mChoiceActionMode;
    MultiChoiceModeWrapper mMultiChoiceModeCallback;

    /**
     * 被选中的item数量
     */
    int mCheckedItemCount;

    /**
     * 存储item的选择状态的键值对集合，key为item在Adapter数据集中对应position，value为true/false，代表是否被选中
     */
    SparseBooleanArray mCheckStates;

    /**
     * 存储被选中的item的键值对集合，key为该item对应的stableId，value为item在Adapter数据集中对应position。
     * 如果Adapter没有启用stableId功能，那么这个变量是无效的。
     */
    LongSparseArray<Integer> mCheckedIdStates;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    /**
     * 手势监听器，用于判断点击、长按、双击、滑动等手势操作
     */
    private RecyclerViewGestureDetector mGestureDetector;

    /**
     * PinnedHeader相关，用于实现悬浮标题头功能
     */
    private RecyclerPinnedHeaderDecoration mRecyclerPinnedHeaderDecoration;


    /**
     * 点击事件回调监听器
     */
    public interface OnItemClickListener {

        /**
         * @param parent
         * @param view
         * @param position
         * @param id
         */
        void onItemClick(RecyclerView parent, View view, int position, long id);
    }


    /**
     * 长按事件回调监听器
     */
    public interface OnItemLongClickListener {
        /**
         * @param parent
         * @param view
         * @param position
         * @param id
         * @return
         */
        boolean onItemLongClick(RecyclerView parent, View view, int position, long id);
    }


    public class FixedViewInfo {
        /**
         * The view to add to the list
         */
        public ViewHolder viewHolder;
        /**
         * <code>true</code> if the fixed view should be selectable in the list
         */
        public boolean isClickable;
    }


    public SXRecyclerView(Context context) {
        this(context, null);
    }

    public SXRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SXRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new ItemGestureDetector(context, new ItemGestureListener());
        mIsSdkVersion21 = android.os.Build.VERSION.SDK_INT >= 21;
        //设置默认点击反馈
        useDefaultSelector();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        Adapter finalAdapter = adapter;
        if (finalAdapter != null) {
            if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
                if (!(adapter instanceof HeaderAndFooterWrapperAdapter)) {
                    finalAdapter = new HeaderAndFooterWrapperAdapter(adapter);
                }
                HeaderAndFooterWrapperAdapter headerAndFooterWrapperAdapter = (HeaderAndFooterWrapperAdapter) finalAdapter;
                for (FixedViewInfo info : mHeaderViewInfos) {
                    headerAndFooterWrapperAdapter.addHeaderView(info);
                }

                for (FixedViewInfo info : mFooterViewInfos) {
                    headerAndFooterWrapperAdapter.addFooterView(info);
                }
            }
        }

        super.setAdapter(finalAdapter);
        if (finalAdapter != null) {
            boolean hasStableIds = getAdapter().hasStableIds();
            if (mChoiceMode != CHOICE_MODE_NONE && hasStableIds &&
                    mCheckedIdStates == null) {
                mCheckedIdStates = new LongSparseArray<>();
            }
        }
        clearChoices();
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        Adapter finalAdapter = adapter;
        if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
            if (!(adapter instanceof HeaderAndFooterWrapperAdapter)) {
                finalAdapter = new HeaderAndFooterWrapperAdapter(adapter);
            }
            HeaderAndFooterWrapperAdapter headerAndFooterWrapperAdapter = (HeaderAndFooterWrapperAdapter) finalAdapter;
            for (FixedViewInfo info : mHeaderViewInfos) {
                headerAndFooterWrapperAdapter.addHeaderView(info);
            }

            for (FixedViewInfo info : mFooterViewInfos) {
                headerAndFooterWrapperAdapter.addFooterView(info);
            }
        }
        super.swapAdapter(finalAdapter, removeAndRecycleExistingViews);
        if (finalAdapter != null) {
            boolean hasStableIds = getAdapter().hasStableIds();
            if (mChoiceMode != CHOICE_MODE_NONE && hasStableIds &&
                    mCheckedIdStates == null) {
                mCheckedIdStates = new LongSparseArray<>();
            }
        }
        clearChoices();
    }

    @Override
    void onStructureChanWhenLayout(boolean change) {
        if (change) {
            confirmCheckedPositionsById();
            setPressed(false);
            if (mSelector != null) {
                mSelector.jumpToCurrentState();
            }
        }
    }

    @Override
    void setupChild(View view, int position) {
        setViewChecked(view, position - getHeaderViewsCount());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        drawSelector(canvas);

        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            mItemDecorations.get(i).onDrawOverChildren(canvas, this, mState);
        }
    }


    @Override
    public void onDrawForeground(Canvas canvas) {
        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            mItemDecorations.get(i).onDrawUnderForeground(canvas, this, mState);
        }
        super.onDrawForeground(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //这个判断是为了在有PinnedHeader的情况下,点击PinnedHeader区域不会去响应普通item的点击长按事件
        if (mRecyclerPinnedHeaderDecoration != null && -1 != mRecyclerPinnedHeaderDecoration.findHeaderPositionUnder((int) e.getX(), (int) e.getY())) {
            return super.onTouchEvent(e);
        }

        boolean flag = mGestureDetector.onTouchEvent(e);
        if (flag) {
            return true;
        }

        return super.onTouchEvent(e);
    }

    /**
     * 注册子view点击事件监听器
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }


    /**
     * 注册子view长按事件监听器
     *
     * @param listener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnItemLongClickListener = listener;
    }


    private class ItemGestureDetector extends RecyclerViewGestureDetector {
        private ItemGestureListener itemGestureListener;
        private int lastTouchX;
        private int lastTouchY;
        private int touchSlop;
        private int scrollPointerId = INVALID_POINTER;
        private VelocityTracker velocityTracker;

        public ItemGestureDetector(Context context, ItemGestureListener listener) {
            super(context, listener);
            itemGestureListener = listener;
            final ViewConfiguration vc = ViewConfiguration.get(context);
            touchSlop = vc.getScaledTouchSlop();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(ev);

            boolean handled = super.onTouchEvent(ev);
            LayoutManager layout = getLayoutManager();
            if (layout == null) {
                return false;
            }
            final boolean canScrollHorizontally = layout.canScrollHorizontally();
            final boolean canScrollVertically = layout.canScrollVertically();
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = (int) (ev.getX() + 0.5f);
                    lastTouchY = (int) (ev.getY() + 0.5f);
                    scrollPointerId = MotionEventCompat.getPointerId(ev, 0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) (ev.getX() + 0.5f);
                    int y = (int) (ev.getY() + 0.5f);
                    int dx = lastTouchX - x;
                    int dy = lastTouchY - y;

                    boolean startScroll = false;
                    if (canScrollHorizontally && Math.abs(dx) > touchSlop) {
                        startScroll = true;
                    }
                    if (canScrollVertically && Math.abs(dy) > touchSlop) {
                        startScroll = true;
                    }
                    if (mTouchMode == TOUCH_MODE_DONE_WAITING && startScroll) {
                        itemGestureListener.startScrolled();
                    }
                    if (startScroll) {
                        lastTouchX = x;
                        lastTouchY = y;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    velocityTracker.computeCurrentVelocity(1000, getMaxFlingVelocity());
                    final float xvel = canScrollHorizontally ?
                            -VelocityTrackerCompat.getXVelocity(velocityTracker, scrollPointerId) : 0;
                    final float yvel = canScrollVertically ?
                            -VelocityTrackerCompat.getYVelocity(velocityTracker, scrollPointerId) : 0;
                    boolean fling = false;
                    if (Math.abs(yvel) >= getMinFlingVelocity() || Math.abs(xvel) >= getMinFlingVelocity()) {
                        fling = true;
                    }
                    if (fling && mTouchMode == TOUCH_MODE_SCROLL) {
                        mTouchMode = TOUCH_MODE_FLING;
                    }
                    itemGestureListener.onUp();
                    if (velocityTracker != null) {
                        velocityTracker.clear();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    itemGestureListener.onCancel();
                    if (velocityTracker != null) {
                        velocityTracker.clear();
                    }
                    break;
            }
            return handled;
        }

    }

    private class ItemGestureListener implements RecyclerViewGestureDetector.OnGestureListener {
        private View clickView;
        private boolean isTapDown = false;

        public void dispatchSingleTapUpIfNeeded(MotionEvent event) {
            if (DEBUG) {
                Log.i(TAG, "dispatchSingleTapUpIfNeeded");
            }
            if (clickView != null) {
                onSingleTapUp(event);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (mTouchMode == TOUCH_MODE_FLING && getScrollState() == SCROLL_STATE_DRAGGING) {
                mTouchMode = TOUCH_MODE_SCROLL;
            } else {
                mTouchMode = TOUCH_MODE_DOWN;
                int dragMotionY = (int) e.getY();
                int dragMotionX = (int) e.getX();

                isTapDown = true;
                clickView = findChildViewUnder(dragMotionX, dragMotionY);
            }

            return clickView != null;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (mTouchMode == TOUCH_MODE_SCROLL) return;
            if (clickView != null) {
                int position = getChildPositionExt(clickView);
                if (position >= 0 && getAdapter().isEnable(position)) {
                    setPressed(true);
                    clickView.setPressed(true);
                    positionSelector(getChildLayoutPosition(clickView), clickView);
                    if (mSelector != null) {
                        if (mIsSdkVersion21) {
                            mSelector.setHotspot(e.getX(), e.getY());
                        }
                    }
                }
            }
            isTapDown = false;
            mTouchMode = TOUCH_MODE_DOWN;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            isTapDown = false;
            if (mTouchMode == TOUCH_MODE_SCROLL) {
                mTouchMode = TOUCH_MODE_FLING;
            }
            mSelectorRect.setEmpty();
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            isTapDown = false;
            if (mTouchMode == TOUCH_MODE_SCROLL) return;
            if (!isLongClickable() || mTouchMode != TOUCH_MODE_DOWN) {
                mTouchMode = TOUCH_MODE_DONE_WAITING;
                return;
            }

            boolean handled = false;
            mTouchMode = TOUCH_MODE_DONE_WAITING;
            if (clickView != null) {
                int position = getChildPositionExt(clickView);
                if (position >= 0) {
                    Long id = getAdapter().getItemId(position);
                    if (getAdapter().isEnable(position) && !mState.didStructureChange()) {
                        handled = performLongPress(SXRecyclerView.this, position, id);
                    }
                }
            }

            if (handled) {
                setPressed(false);
                clickView.setPressed(false);
                clickView = null;
                mSelectorRect.setEmpty();
                mTouchMode = TOUCH_MODE_REST;
            } else {
                mTouchMode = TOUCH_MODE_DONE_WAITING;
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //长按后是不会继续执行onScroll
            isTapDown = false;

            startScrolled();

            return false;
        }

        public void startScrolled() {
            setPressed(false);
            if (clickView != null) {
                clickView.setPressed(false);
            }
            if (mSelector != null) {
                mSelector.jumpToCurrentState();
            }
            mSelectorRect.setEmpty();

            mTouchMode = TOUCH_MODE_SCROLL;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            boolean handled = false;

            if (clickView != null && !mState.didStructureChange() && mTouchMode == TOUCH_MODE_DOWN && getAdapter().isEnable(getChildLayoutPosition(clickView))) {
                int time = isTapDown ? ViewConfiguration.getPressedStateDuration() : 0;

                if (isTapDown) {
                    setPressed(true);
                    clickView.setPressed(true);
                    positionSelector(getChildLayoutPosition(clickView), clickView);

                    if (mSelector != null) {
                        if (mIsSdkVersion21) {
                            mSelector.setHotspot(e.getX(), e.getY());
                        }
                    }
                }

                if (mOnItemClickListener != null) {
                    handled = true;
                }

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (clickView != null) {
                            setPressed(false);
                            clickView.setPressed(false);
                            int position = getChildPositionExt(clickView);
                            if (position >= 0 && !mState.didStructureChange() && isAttachedToWindow()) {
                                Long id = getAdapter().getItemId(position);
                                performItemClick(SXRecyclerView.this, clickView, position, id);
                            }
                            clickView = null;
                            isTapDown = false;
                        }
                    }
                }, time);
                mTouchMode = TOUCH_MODE_REST;
            }
            return handled;
        }

        public boolean onUp() {
            boolean handled = false;
            if (clickView != null && !isTapDown) {
                setPressed(false);
                clickView.setPressed(false);
            }

            if (mTouchMode == TOUCH_MODE_DONE_WAITING) {
                int position = getChildPositionExt(clickView);
                if (position >= 0 && !mState.didStructureChange() && isAttachedToWindow() && getAdapter().isEnable(position)) {
                    Long id = getAdapter().getItemId(position);
                    performItemClick(SXRecyclerView.this, clickView, position, id);
                }

            }
            if (mTouchMode == TOUCH_MODE_SCROLL || mTouchMode == TOUCH_MODE_DOWN || mTouchMode == TOUCH_MODE_DONE_WAITING) {
                mTouchMode = TOUCH_MODE_REST;
            }
            return handled;
        }

        public void onCancel() {

            mTouchMode = TOUCH_MODE_REST;
            setScrollState(SCROLL_STATE_IDLE);
            if (clickView != null) {
                clickView.setPressed(false);
            }
            setPressed(false);
        }

        /**
         * @param recyclerView
         * @param view         点击的itemview
         * @param position     itemview在界面上对应的position，数值上等于对应item在adapter中的position + headerview的数量
         * @param id           itemview对应的item的stableId，需要正确启用stableId功能该数值才有效
         * @return 是否消费掉此次事件
         */
        private boolean performItemClick(RecyclerView recyclerView, View view, int position, long id) {
            boolean handled = false;
            boolean dispatchItemClick = true;

            if (isHeaderOrFooter(position)) {
                return true;
            }

            int adjPosition = position - getHeaderViewsCount();

            if (mChoiceMode != CHOICE_MODE_NONE) {
                handled = true;
                boolean checkedStateChanged = false;
                final Adapter adapter = recyclerView.getAdapter();
                if (mChoiceMode == CHOICE_MODE_MULTIPLE && mChoiceActionMode != null && adapter.isSelectable(position)) {

                    boolean checked = !mCheckStates.get(adjPosition, false);
                    mCheckStates.put(adjPosition, checked);

                    if (mCheckedIdStates != null && adapter.hasStableIds()) {
                        if (checked) {
                            mCheckedIdStates.put(adapter.getItemId(position), adjPosition);
                        } else {
                            mCheckedIdStates.delete(adapter.getItemId(position));
                        }
                    }
                    if (checked) {
                        mCheckedItemCount++;
                    } else {
                        mCheckedItemCount--;
                    }
                    if (mChoiceActionMode != null) {
                        mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                                adjPosition, id, checked);
                        dispatchItemClick = false;
                    }
                    checkedStateChanged = true;
                }

                if (checkedStateChanged) {
                    updateOnScreenCheckedViews();
                }
            }

            if (dispatchItemClick) {
                if (mOnItemClickListener != null) {
                    recyclerView.playSoundEffect(SoundEffectConstants.CLICK);
                    mOnItemClickListener.onItemClick(recyclerView, view, adjPosition, id);
                    view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
                    handled = true;
                }
            }
            return handled;
        }

        /**
         * @param recyclerView
         * @param longPressPosition 被长按的itemview在界面上对应的position，数值上等于对应item在adapter中的position + headerview的数量
         * @param longPressId       itemview对应的item的stableId，需要正确启用stableId功能该数值才有效
         * @return 是否消费掉此次事件
         */
        private boolean performLongPress(RecyclerView recyclerView, final int longPressPosition, final long longPressId) {
            if (isHeaderOrFooter(longPressPosition)){
                return true;
            }
            int adjPosition = longPressPosition - getHeaderViewsCount();

            if (mChoiceMode == CHOICE_MODE_MULTIPLE && recyclerView.getAdapter().isSelectable(longPressPosition)) {
                if (mChoiceActionMode == null &&
                        (mChoiceActionMode = recyclerView.startActionMode(mMultiChoiceModeCallback)) != null) {
                    setItemChecked(longPressPosition, true);
                    recyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    updateOnScreenCheckedViews();
                }
                return true;
            }

            boolean handled = false;
            if (mOnItemLongClickListener != null) {
                handled = mOnItemLongClickListener.onItemLongClick(recyclerView, clickView, adjPosition, longPressId);
            }
            if (handled) {
                recyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            return handled;
        }

    }


    /**
     * 取得选中的Item数量
     *
     * @return
     */
    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }


    /**
     * 判断该数据项是否被选中
     *
     * @param position
     * @return
     */
    public boolean isItemChecked(int position) {
        if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
            return mCheckStates.get(position);
        }

        return false;
    }

    /**
     * 取得选中Item的position集合
     *
     * @return
     */
    public List<Integer> getCheckedItemPositions() {
        if (mChoiceMode == CHOICE_MODE_NONE || mCheckStates == null || getAdapter() == null) {
            return null;
        }

        final SparseBooleanArray posStates = mCheckStates;
        List<Integer> checkedPositions = new ArrayList<>();

        for (int i = 0; i < posStates.size(); i++) {
            if (posStates.valueAt(i)) {
                checkedPositions.add(posStates.keyAt(i));
            }
        }

        return checkedPositions;
    }

    /**
     * 取得选中Item的ID数组集合
     * 该方法必须在adapter有stable ID 的情况下才有效
     *
     * @return
     */
    public long[] getCheckedItemIds() {
        if (mChoiceMode == CHOICE_MODE_NONE || mCheckedIdStates == null || getAdapter() == null) {
            return new long[0];
        }

        final LongSparseArray<Integer> idStates = mCheckedIdStates;
        final int count = idStates.size();
        final long[] ids = new long[count];

        for (int i = 0; i < count; i++) {
            ids[i] = idStates.keyAt(i);
        }

        return ids;
    }

    /**
     * 清除之前所有的选择状态
     */
    public void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        mCheckedItemCount = 0;
    }

    /**
     * 设置对应位置数据项的选择状态
     *
     * @param position 界面上itemview对应的position，数值上等于对应item在adapter中的position + headerview的数量
     * @param value    选中状态
     */
    public void setItemChecked(int position, boolean value) {
        if (mChoiceMode == CHOICE_MODE_NONE) {
            return;
        }

        final Adapter adapter = getAdapter();

        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value && mChoiceMode == CHOICE_MODE_MULTIPLE && mChoiceActionMode == null) {
            if (mMultiChoiceModeCallback == null ||
                    !mMultiChoiceModeCallback.hasWrappedCallback()) {
                throw new IllegalStateException("RecyclerView: attempted to start selection mode " +
                        "for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was " +
                        "supplied. Call setMultiChoiceModeListener to set a callback.");
            }
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
        }

        int adjPosition = position - getHeaderViewsCount();

        if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
            boolean oldValue = mCheckStates.get(adjPosition);
            mCheckStates.put(adjPosition, value);
            if (mCheckedIdStates != null && adapter.hasStableIds()) {
                if (value) {
                    mCheckedIdStates.put(adapter.getItemId(position), adjPosition);
                } else {
                    mCheckedIdStates.delete(adapter.getItemId(position));
                }
            }
            if (oldValue != value) {
                if (value) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
            }
            if (mChoiceActionMode != null) {
                final long id = adapter.getItemId(position);
                mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                        adjPosition, id, value);
            }
        } else {
            boolean updateIds = mCheckedIdStates != null && adapter.hasStableIds();
            // Clear all values if we're checking something, or unchecking the currently
            // selected item
            if (value || isItemChecked(adjPosition)) {
                mCheckStates.clear();
                if (updateIds) {
                    mCheckedIdStates.clear();
                }
            }
            // this may end up selecting the value we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (value) {
                mCheckStates.put(adjPosition, true);
                if (updateIds) {
                    mCheckedIdStates.put(adapter.getItemId(position), adjPosition);
                }
                mCheckedItemCount = 1;
            } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                mCheckedItemCount = 0;
            }
        }

        updateOnScreenCheckedViews();
    }

    /**
     * Perform a quick, in-place update of the checked or activated state
     * on all visible item views. This should only be called when a valid
     * choice mode is active.
     */
    private void updateOnScreenCheckedViews() {
        final int count = getChildCountExt();
        for (int i = 0; i < count; i++) {
            final View child = getChildAtExt(i);
            final int position = getChildPositionExt(child);
            setViewChecked(child, position - getHeaderViewsCount());
        }
    }

    public void setViewChecked(View child, int position) {
        if (child == null) {
            return;
        }
        if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
            boolean checked = mCheckStates.get(position);
            ViewHolder viewHolder = getChildViewHolder(child);
            if (viewHolder instanceof Checkable) {
                ((Checkable) viewHolder).setChecked(checked);
            }
        }
    }

    /**
     * 取得当前的选择模式
     *
     * @return
     */
    public int getChoiceMode() {
        return mChoiceMode;
    }


    /**
     * 设置选择模式
     *
     * @param choiceMode
     */
    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        if (mChoiceActionMode != null) {
            mChoiceActionMode.finish();
            mChoiceActionMode = null;
        }
        if (mChoiceMode != CHOICE_MODE_NONE) {
            if (mCheckStates == null) {
                mCheckStates = new SparseBooleanArray(0);
            }
            final Adapter adapter = getAdapter();
            if (mCheckedIdStates == null && adapter != null && adapter.hasStableIds()) {
                mCheckedIdStates = new LongSparseArray<Integer>(0);
            }
            // Modal multi-choice mode only has choices when the mode is active. Clear them.
            if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
                clearChoices();
                setLongClickable(true);
            }
        }
    }

    /**
     * Set a {@link MultiChoiceModeListener} that will manage the lifecycle of the
     * selection {@link ActionMode}. Only used when the choice mode is set to
     * {@link #CHOICE_MODE_MULTIPLE}.
     *
     * @param listener Listener that will manage the selection mode
     * @see #setChoiceMode(int)
     */
    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (mMultiChoiceModeCallback == null) {
            mMultiChoiceModeCallback = new MultiChoiceModeWrapper();
        }
        mMultiChoiceModeCallback.setWrapped(listener);
    }

    /**
     * A MultiChoiceModeListener receives events for {@link #CHOICE_MODE_MULTIPLE}.
     * It acts as the {@link ActionMode.Callback} for the selection mode and also receives
     * {@link #onItemCheckedStateChanged(ActionMode, int, long, boolean)} events when the user
     * selects and deselects list items.
     */
    public interface MultiChoiceModeListener extends ActionMode.Callback {
        /**
         * Called when an item is checked or unchecked during selection mode.
         *
         * @param mode     The {@link ActionMode} providing the selection mode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  <code>true</code> if the item is now checked, <code>false</code>
         *                 if the item is now unchecked.
         */
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked);
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {
        private MultiChoiceModeListener mWrapped;

        public void setWrapped(MultiChoiceModeListener wrapped) {
            mWrapped = wrapped;
        }

        public boolean hasWrappedCallback() {
            return mWrapped != null;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (mWrapped.onCreateActionMode(mode, menu)) {
                // Initialize checked graphic state?
                if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
                    //allow long click after selection mode
                    setLongClickable(true);
                } else {
                    setLongClickable(false);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mWrapped.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrapped.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mWrapped.onDestroyActionMode(mode);
            mChoiceActionMode = null;

            // Ending selection mode means deselecting everything.
            clearChoices();

            updateOnScreenCheckedViews();

            setLongClickable(true);

            //退出多选时清掉之前的缓存,原因是这些缓存的ViewHolder中的CheckBox状态还是多选时的状态,复用这些缓存时,AnimCheckbox的初始状态是错误的,从而导致动画异常,故这些缓存不能用
            mRecycler.clear();
            getRecycledViewPool().clear();
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked) {
            mWrapped.onItemCheckedStateChanged(mode, position, id, checked);

            // If there are no items selected we no longer need the selection mode.
//            if (getCheckedItemCount() == 0) {
//                mode.finish();
//            }
        }
    }


    /**
     * 校正操作，只有在启用了stableId功能时才有效
     */
    void confirmCheckedPositionsById() {
        final Adapter adapter = getAdapter();
        if (mChoiceMode == CHOICE_MODE_NONE || adapter == null || !adapter.hasStableIds()) {
            return;
        }
        final int itemCount = getItemCount();
        // Clear out the positional check states, we'll rebuild it below from IDs.
        mCheckStates.clear();

        boolean checkedCountChanged = false;
        for (int checkedIndex = 0; checkedIndex < mCheckedIdStates.size(); checkedIndex++) {
            final long id = mCheckedIdStates.keyAt(checkedIndex);
            final int lastPos = mCheckedIdStates.valueAt(checkedIndex);

            //由于adapter中会对position进行headerview数量的矫正，所以传给adapter的需要是包含headerview数量的position
            //而lastPos是已经进行过headerview数量矫正的，所以需要还原成未矫正前的数值，避免重复矫正导致计算出错
            final long lastPosId = adapter.getItemId(lastPos + getHeaderViewsCount());
            if (id != lastPosId) {
                // Look around to see if the ID is nearby. If not, uncheck it.
                final int start = Math.max(0, lastPos - CHECK_POSITION_SEARCH_DISTANCE);
                final int end = Math.min(lastPos + CHECK_POSITION_SEARCH_DISTANCE, itemCount);
                boolean found = false;
                for (int searchPos = start; searchPos < end; searchPos++) {
                    final long searchId = adapter.getItemId(searchPos);
                    if (id == searchId) {
                        found = true;
                        int adjPosition = searchPos - getHeaderViewsCount();
                        mCheckStates.put(adjPosition, true);
                        mCheckedIdStates.setValueAt(checkedIndex, adjPosition);
                        break;
                    }
                }

                if (!found) {
                    mCheckedIdStates.delete(id);
                    checkedIndex--;
                    mCheckedItemCount--;
                    checkedCountChanged = true;
                    if (mChoiceActionMode != null && mMultiChoiceModeCallback != null) {
                        mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                                lastPos, id, false);
                    }
                }
            } else {
                mCheckStates.put(lastPos, true);
            }
        }

        if (checkedCountChanged && mChoiceActionMode != null) {
            mChoiceActionMode.invalidate();
        }
    }

    public void finishMultiChoice() {
        if (mChoiceActionMode != null)
            mChoiceActionMode.finish();
    }

    public boolean startMultiChoice() {
        if (mChoiceMode == CHOICE_MODE_MULTIPLE
                && mChoiceActionMode == null) {
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);

            if (mChoiceActionMode == null)
                return false;


            post(new Runnable() {
                public void run() {
                    requestLayout();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 判断Recyclerview是否处于多选状态
     *
     * @return
     */
    public Boolean isInMutiChoiceState() {
        return mChoiceActionMode != null;
    }


    protected int findCandidateScrollSelection(boolean up) {
        int childCount = getChildCountExt();
        int position = INVALID_POSITION;
        int firstPosition = getFirstPosition();
        if (up) {
            for (int index = 0; index < childCount; index++) {
                View child = getChildAtExt(index);
                if (getDecoratedBottom(child) - getDecoratedMeasuredHeight(child) / 2 > getPaddingTop()) {
                    position = firstPosition + index;
                    break;
                }
            }
        } else {
            for (int index = childCount - 1; index >= 0; index--) {
                View child = getChildAtExt(index);
                if (getDecoratedTop(child) + getDecoratedMeasuredHeight(child) / 2 < (getHeight() - getPaddingBottom())) {
                    position = firstPosition + index;
                    break;
                }
            }
        }
        return position;
    }


    public int getFirstPosition() {
        View view = getChildAtExt(0);
        if (view == null) {
            return INVALID_POSITION;
        }
        return getChildPositionExt(view);
    }

    public int getLastPosition() {
        View view = getChildAtExt(getChildCountExt() - 1);
        if (view == null) {
            return INVALID_POSITION;
        }
        return getChildPositionExt(view);
    }

    private int getChildCountExt() {
        return mChildHelper != null ? mChildHelper.getChildCount() : 0;
    }

    public View getChildAtExt(int index) {
        return mChildHelper != null ? mChildHelper.getChildAt(index) : null;
    }

    private int getChildPositionExt(View view) {
        if (view == null) {
            return INVALID_POSITION;
        }
        return getChildLayoutPosition(view);
    }

    public int getCount() {
        return getAdapter().getItemCount();
    }

    private int getItemCount() {
        final Adapter a = getAdapter();
        return a != null ? a.getItemCount() : 0;
    }

    public int getPositionForView(View view) {
        View listItem = view;
        try {
            View v;
            while (!(v = (View) listItem.getParent()).equals(this)) {
                listItem = v;
            }
        } catch (ClassCastException e) {
            // We made it up to the window without find this list view
            return INVALID_POSITION;
        }

        // Search the children for the list item
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).equals(listItem)) {
                return getFirstPosition() + i;
            }
        }
        // Child not found!
        return INVALID_POSITION;

    }

    private void trackMotionScroll(int incrementalDeltaY, boolean offsetChild) {
        int childCount = getChildCountExt();
        if (childCount == 0) {
            return;
        }
        if (offsetChild) {
            offsetChildrenVertical(incrementalDeltaY);
        }

        if (!awakenScrollBars()) {
            invalidate();
        }
    }

    public int getDecoratedBottom(View view) {
        LayoutManager layoutManager = getLayoutManager();
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return layoutManager.getDecoratedBottom(view) + params.bottomMargin;
    }

    public int getDecoratedTop(View view) {
        LayoutManager layoutManager = getLayoutManager();
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return layoutManager.getDecoratedTop(view) - params.topMargin;
    }

    public int getDecoratedMeasuredHeight(View view) {
        LayoutManager layoutManager = getLayoutManager();
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return layoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
    }


    protected void invalidateParentIfNeeded() {
        if (isHardwareAccelerated() && getParent() instanceof View) {
            ((View) getParent()).invalidate();
        }
    }

    public void checkedAll() {
        int itemCount = getItemCount();
        Adapter adapter = getAdapter();
        if (itemCount == 0 || adapter == null) {
            return;
        }
        int firtFooterPosition = getItemCount() - getFooterViewsCount();
        clearChoices();
        if (mChoiceMode != CHOICE_MODE_NONE) {
            for (int position = getHeaderViewsCount(); position < firtFooterPosition; position++) {
                if (adapter.isEnable(position) && adapter.isSelectable(position)) {
                    int adjPosition = position - getHeaderViewsCount();
                    mCheckStates.put(adjPosition, true);
                    if (mCheckedIdStates != null && adapter.hasStableIds()) {
                        mCheckedIdStates.put(adapter.getItemId(position), adjPosition);
                    }
                    mCheckedItemCount++;
                }

            }
        }

        requestLayout();
    }

    public void unCheckedAll() {
        clearChoices();
        requestLayout();
    }

    private void useDefaultSelector() {
        setSelector(getResources().getDrawable(R.drawable.item_click_background));
    }

    public void setSelector(int resID) {
        setSelector(getResources().getDrawable(resID));
    }

    public void setSelector(Drawable sel) {
        if (mSelector != null) {
            mSelector.setCallback(null);
            unscheduleDrawable(mSelector);
        }
        if (sel == null) {
            mSelector = null;
            return;
        }
        mSelector = sel;
        Rect padding = new Rect();
        sel.getPadding(padding);
        mSelectionLeftPadding = padding.left;
        mSelectionTopPadding = padding.top;
        mSelectionRightPadding = padding.right;
        mSelectionBottomPadding = padding.bottom;
        sel.setCallback(this);
        updateSelectorState();

    }

    void updateSelectorState() {
        if (mSelector != null) {
            if (shouldShowSelector()) {
                mSelector.setState(getDrawableState());
            } else {
                mSelector.setState(StateSet.NOTHING);
            }
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateSelectorState();
    }

    @Override
    public boolean verifyDrawable(Drawable dr) {
        return mSelector == dr || super.verifyDrawable(dr);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mSelector != null) mSelector.jumpToCurrentState();
    }

    boolean shouldShowSelector() {
        return (isFocused() && !isInTouchMode()) || isPressed();
    }

    protected void drawSelector(Canvas canvas) {
        if (!mSelectorRect.isEmpty()) {
            final Drawable selector = mSelector;
            selector.setBounds(mSelectorRect);
            selector.draw(canvas);
        }
    }

    void positionSelector(int position, View sel) {
        final boolean positionChanged = position != mSelectorPosition;
        if (position != INVALID_POSITION) {
            mSelectorPosition = position;
        }

        final Rect selectorRect = mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        // Adjust for selection padding.
        selectorRect.left -= mSelectionLeftPadding;
        selectorRect.top -= mSelectionTopPadding;
        selectorRect.right += mSelectionRightPadding;
        selectorRect.bottom += mSelectionBottomPadding;

        // Update the selector drawable.
        final Drawable selector = mSelector;
        if (selector != null) {
            if (positionChanged) {
                // Wipe out the current selector state so that we can start
                // over in the new position with a fresh state.
                selector.setVisible(false, false);
                selector.setState(StateSet.NOTHING);
            }
            selector.setBounds(selectorRect);
            if (positionChanged) {
                if (getVisibility() == VISIBLE) {
                    selector.setVisible(true, false);
                }
                updateSelectorState();
            }
        }

        refreshDrawableState();
    }


    /**
     * 设置PinnedHeader,用于区分PinnedHeader与普通item的touch事件
     *
     * @param pinnedHeaderDecoration
     */
    public void setPinnedHeaderDecoration(RecyclerPinnedHeaderDecoration pinnedHeaderDecoration) {
        mRecyclerPinnedHeaderDecoration = pinnedHeaderDecoration;
    }


    public void addHeaderView(ViewHolder viewHolder) {
        addHeaderView(viewHolder, true);
    }

    public void addHeaderView(ViewHolder viewHolder, boolean isSelectable) {
        if (null == viewHolder) {
            throw new IllegalArgumentException("the viewHolder to add must not be null!");
        }

        final FixedViewInfo info = new FixedViewInfo();
        info.viewHolder = viewHolder;
        info.isClickable = isSelectable;
        mHeaderViewInfos.add(info);

        if (mAdapter != null) {
            if (!(mAdapter instanceof HeaderAndFooterWrapperAdapter)) {
                mAdapter.unregisterAdapterDataObserver(mObserver);
                mAdapter = new HeaderAndFooterWrapperAdapter(mAdapter);
                mAdapter.registerAdapterDataObserver(mObserver);
                ((HeaderAndFooterWrapperAdapter) mAdapter).setHeaderAndFooterSpanForGridLayoutManager(this);
            }
            ((HeaderAndFooterWrapperAdapter) mAdapter).addHeaderView(info);

            ((HeaderAndFooterWrapperAdapter) mAdapter).getWrappedAdapter().notifyDataSetChanged();
        }
    }

    public int getHeaderViewsCount() {
        if (mAdapter != null && mAdapter instanceof HeaderAndFooterWrapperAdapter) {
            return ((HeaderAndFooterWrapperAdapter) mAdapter).getHeadersCount();
        }
        return 0;
    }

    public boolean removeHeaderView(ViewHolder viewHolder) {
        boolean result = false;
        if (mAdapter != null && mAdapter instanceof HeaderAndFooterWrapperAdapter) {
            FixedViewInfo info = removeFixedViewInfo(viewHolder, mHeaderViewInfos);
            result = ((HeaderAndFooterWrapperAdapter) mAdapter).removeHeaderView(info);
            if (result) {
                ((HeaderAndFooterWrapperAdapter) mAdapter).getWrappedAdapter().notifyDataSetChanged();
            }
        }
        return result;
    }

    public void addFooterView(ViewHolder viewHolder) {
        addFooterView(viewHolder, true);
    }

    public void addFooterView(ViewHolder viewHolder, boolean isSelectable) {
        if (null == viewHolder) {
            throw new IllegalArgumentException("the viewHolder to add must not be null!");
        }

        final FixedViewInfo info = new FixedViewInfo();
        info.viewHolder = viewHolder;
        info.isClickable = isSelectable;
        mFooterViewInfos.add(info);

        if (mAdapter != null) {
            if (!(mAdapter instanceof HeaderAndFooterWrapperAdapter)) {
                mAdapter.unregisterAdapterDataObserver(mObserver);
                mAdapter = new HeaderAndFooterWrapperAdapter(mAdapter);
                mAdapter.registerAdapterDataObserver(mObserver);
                ((HeaderAndFooterWrapperAdapter) mAdapter).setHeaderAndFooterSpanForGridLayoutManager(this);
            }
            ((HeaderAndFooterWrapperAdapter) mAdapter).addFooterView(info);

            ((HeaderAndFooterWrapperAdapter) mAdapter).getWrappedAdapter().notifyDataSetChanged();
        }
    }

    public int getFooterViewsCount() {
        if (mAdapter != null && mAdapter instanceof HeaderAndFooterWrapperAdapter) {
            return ((HeaderAndFooterWrapperAdapter) mAdapter).getFootersCount();
        }
        return 0;
    }

    public boolean removeFooterView(ViewHolder viewHolder) {
        boolean result = false;
        if (mAdapter != null && mAdapter instanceof HeaderAndFooterWrapperAdapter) {
            FixedViewInfo info = removeFixedViewInfo(viewHolder, mFooterViewInfos);
            result = ((HeaderAndFooterWrapperAdapter) mAdapter).removeFooterView(info);
            if (result) {
                ((HeaderAndFooterWrapperAdapter) mAdapter).getWrappedAdapter().notifyDataSetChanged();
            }
        }
        return result;
    }

    private FixedViewInfo removeFixedViewInfo(ViewHolder viewHolder, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        FixedViewInfo viewInfo = null;
        for (int i = 0; i < len; ++i) {
            FixedViewInfo info = where.get(i);
            if (info.viewHolder == viewHolder) {
                where.remove(i);
                viewInfo = info;
                break;
            }
        }
        return viewInfo;
    }

    protected boolean isHeaderOrFooter(int position) {
        return position >= 0 &&
                (position < getHeaderViewsCount() || (position >= getItemCount() - getFooterViewsCount()));
    }


    @Override
    public void addItemDecoration(ItemDecoration decor) {

        super.addItemDecoration(decor);
    }

    @Override
    public void addItemDecoration(ItemDecoration decor, int index) {
        if (decor instanceof RecyclerPinnedHeaderDecoration) {
            mRecyclerPinnedHeaderDecoration = (RecyclerPinnedHeaderDecoration) decor;
        }
        super.addItemDecoration(decor, index);
    }

    @Override
    public void removeItemDecoration(ItemDecoration decor) {
        if (decor instanceof RecyclerPinnedHeaderDecoration) {
            mRecyclerPinnedHeaderDecoration = null;
        }
        super.removeItemDecoration(decor);

    }


}
