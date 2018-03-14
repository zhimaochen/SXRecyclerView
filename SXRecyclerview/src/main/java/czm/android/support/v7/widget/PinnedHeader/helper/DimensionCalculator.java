package czm.android.support.v7.widget.PinnedHeader.helper;

import android.graphics.Rect;
import android.view.View;

import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.MarginLayoutParams;

/**
 * View尺寸计算的辅助类
 */
public class DimensionCalculator {

    /**
     * 设置 {@link View} 的 {@link MarginLayoutParams}
     *
     * @param margins rect to populate
     * @param view    for which to get margins
     */
    public void initMargins(Rect margins, View view) {
        LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof MarginLayoutParams) {
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
            initMarginRect(margins, marginLayoutParams);
        } else {
            margins.set(0, 0, 0, 0);
        }
    }

    private void initMarginRect(Rect marginRect, MarginLayoutParams marginLayoutParams) {
        marginRect.set(
                marginLayoutParams.leftMargin,
                marginLayoutParams.topMargin,
                marginLayoutParams.rightMargin,
                marginLayoutParams.bottomMargin
        );
    }
}
