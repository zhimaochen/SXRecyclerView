package czm.android.support.v7.widget.PinnedHeader.helper;



import czm.android.support.v7.widget.LinearLayoutManager;
import czm.android.support.v7.widget.SXRecyclerView;

public class LinearLayoutOrientationProvider implements OrientationProvider {

    @Override
    public int getOrientation(SXRecyclerView recyclerView) {
        SXRecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        throwIfNotLinearLayoutManager(layoutManager);
        return ((LinearLayoutManager) layoutManager).getOrientation();
    }

    @Override
    public boolean isReverseLayout(SXRecyclerView recyclerView) {
        SXRecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        throwIfNotLinearLayoutManager(layoutManager);
        return ((LinearLayoutManager) layoutManager).getReverseLayout();
    }

    private void throwIfNotLinearLayoutManager(SXRecyclerView.LayoutManager layoutManager) {
        if (!(layoutManager instanceof LinearLayoutManager)) {
            String ex = "SXRecyclerView PinnedHeader decoration can only be used with a " +
                    "LinearLayoutManager.";
            throw new IllegalStateException(ex);
        }
    }
}
