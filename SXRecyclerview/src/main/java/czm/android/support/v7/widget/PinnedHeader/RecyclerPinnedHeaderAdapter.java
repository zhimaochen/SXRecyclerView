package czm.android.support.v7.widget.PinnedHeader;

import android.view.ViewGroup;

import czm.android.support.v7.widget.SXRecyclerView;


public interface RecyclerPinnedHeaderAdapter<VH extends SXRecyclerView.ViewHolder> {

    /**
     * PinnedHeader 的编号 id,该 id 一般情况下是指 要显示在 PinnedHeader 上的字符 如 'A','B','#'... 转换成 long 后的值
     *
     * @param position
     * @return 返回 负数 的时候，表示该 PinnedHeader 不显示，例如 HeaderView 和 FooterView 就不该有 PinnedHeader
     */
    long getHeaderId(int position);

    /**
     * 创建 HeaderViewHolder
     *
     * @param parent
     * @return
     */
    VH onCreateHeaderViewHolder(ViewGroup parent);

    /**
     * Bind HeaderViewHolder
     *
     * @param holder
     * @param position
     */
    void onBindHeaderViewHolder(VH holder, int position);

    /**
     * @return
     */
    int getItemCount();
}