package czm.android.support.v7.widget.PinnedHeader.helper;


import android.view.View;

import czm.android.support.v7.widget.SXRecyclerView;

/**
 * Implemented by objects that provide header views for decoration
 */
public interface PinnedHeaderProvider {

    /**
     * This will provide a pinned header view for a given position in the MzRecyclerView
     *
     * @param recyclerView that will display the header
     * @param position     that will be headed by the header
     * @return a header view for the given position and list
     */
    View getHeader(SXRecyclerView recyclerView, int position);

    /**
     * TODO: describe this functionality and its necessity
     */
    void invalidate();
}
