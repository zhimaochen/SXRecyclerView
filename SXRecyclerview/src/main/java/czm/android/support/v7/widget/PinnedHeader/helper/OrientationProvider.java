package czm.android.support.v7.widget.PinnedHeader.helper;


import czm.android.support.v7.widget.SXRecyclerView;

/**
 * 横竖屏和数据朝向接口
 */
public interface OrientationProvider {

    /**
     * Orientation 的接口,可以设置横屏和竖屏
     */
    int getOrientation(SXRecyclerView recyclerView);

    /**
     * 设置 RecyclerView 中数据的排列，
     *
     * @param recyclerView
     * @return false if 数据是正向排列(A-Z);
     * true if 数据是反向排列(Z-A)
     */
    boolean isReverseLayout(SXRecyclerView recyclerView);
}
