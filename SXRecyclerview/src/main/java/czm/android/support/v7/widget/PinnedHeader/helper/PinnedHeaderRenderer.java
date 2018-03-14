package czm.android.support.v7.widget.PinnedHeader.helper;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.widget.LinearLayout;

import czm.android.support.v7.widget.SXRecyclerView;

/**
 * Draw headers to the canvas provided by the item decoration
 */
public class PinnedHeaderRenderer {

    /**
     * 用來计算 PinnedHeader 的尺寸
     */
    private final DimensionCalculator mDimensionCalculator;

    /**
     * Orientation of PinnedHeader
     */
    private final OrientationProvider mOrientationProvider;

    /**
     * PinnedHeader 绘制时临时的 Rect，所有的 PinnedHeader 都使用这个 Rect
     */
    private final Rect mTempRect = new Rect();

    public PinnedHeaderRenderer(OrientationProvider orientationProvider) {
        this(orientationProvider, new DimensionCalculator());
    }

    private PinnedHeaderRenderer(OrientationProvider orientationProvider,
                                 DimensionCalculator dimensionCalculator) {
        mOrientationProvider = orientationProvider;
        mDimensionCalculator = dimensionCalculator;
    }

    /**
     * Draw a PinnedHeader
     *
     * @param recyclerView the parent recycler view for drawing the header into
     * @param canvas       the canvas on which to draw the header
     * @param header       the view to draw as the header
     * @param offset       a Rect used to define the x/y offset of the header. Specify x/y offset by setting
     *                     the {@link Rect#left} and {@link Rect#top} properties, respectively.
     */
    public void drawHeader(SXRecyclerView recyclerView, Canvas canvas, View header, Rect offset) {
        canvas.save();

        if (recyclerView.getLayoutManager().getClipToPadding()) {
            initClipRectForHeader(mTempRect, recyclerView, header);
            canvas.clipRect(mTempRect);
        }

        canvas.translate(offset.left, offset.top);

        header.draw(canvas);
        canvas.restore();
    }

    /**
     * Initializes a clipping rect for the header based on the margins of the header and the padding of the
     * recycler.
     * FIXME: Currently right margin in VERTICAL orientation and bottom margin in HORIZONTAL
     * orientation are clipped so they look accurate, but the headers are not being drawn at the
     * correctly smaller width and height respectively.
     *
     * @param clipRect     {@link Rect} for clipping a provided header to the padding of a recycler view
     * @param recyclerView for which to provide a header
     * @param header       for clipping
     */
    private void initClipRectForHeader(Rect clipRect, SXRecyclerView recyclerView, View header) {
        mDimensionCalculator.initMargins(clipRect, header);
        if (mOrientationProvider.getOrientation(recyclerView) == LinearLayout.VERTICAL) {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight() - clipRect.right,
                    recyclerView.getHeight() - recyclerView.getPaddingBottom());
        } else {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight(),
                    recyclerView.getHeight() - recyclerView.getPaddingBottom() - clipRect.bottom);
        }
    }

}
