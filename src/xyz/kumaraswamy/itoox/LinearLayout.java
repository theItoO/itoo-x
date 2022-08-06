package xyz.kumaraswamy.itoox;

import com.google.appinventor.components.common.ComponentConstants;

import android.content.Context;
import android.view.ViewGroup;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Layout;

public final class LinearLayout implements Layout {

    private final android.widget.LinearLayout layoutManager;

    public LinearLayout(Context context, int orientation) {
        this(context, orientation, null, null);
    }

    /**
     * Creates a new linear layout with a preferred empty width/height.
     *
     * @param context  view context
     * @param orientation one of
     *     {@link ComponentConstants#LAYOUT_ORIENTATION_HORIZONTAL}.
     *     {@link ComponentConstants#LAYOUT_ORIENTATION_VERTICAL}
     * @param preferredEmptyWidth the preferred width of an empty layout
     * @param preferredEmptyHeight the preferred height of an empty layout
     */
    LinearLayout(Context context, int orientation, final Integer preferredEmptyWidth,
                 final Integer preferredEmptyHeight) {
        if (preferredEmptyWidth == null && preferredEmptyHeight != null ||
                preferredEmptyWidth != null && preferredEmptyHeight == null) {
            throw new IllegalArgumentException("LinearLayout - preferredEmptyWidth and " +
                    "preferredEmptyHeight must be either both null or both not null");
        }

        layoutManager = new android.widget.LinearLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (preferredEmptyWidth == null) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    return;
                }

                if (getChildCount() != 0) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    return;
                }

                setMeasuredDimension(getSize(widthMeasureSpec, preferredEmptyWidth),
                        getSize(heightMeasureSpec, preferredEmptyHeight));
            }

            private int getSize(int measureSpec, int preferredSize) {
                int result;
                int specMode = MeasureSpec.getMode(measureSpec);
                int specSize = MeasureSpec.getSize(measureSpec);

                if (specMode == MeasureSpec.EXACTLY) {
                    // We were told how big to be
                    result = specSize;
                } else {
                    // Use the preferred size.
                    result = preferredSize;
                    if (specMode == MeasureSpec.AT_MOST) {
                        // Respect AT_MOST value if that was what is called for by measureSpec
                        result = Math.min(result, specSize);
                    }
                }

                return result;
            }
        };

        layoutManager.setOrientation(
                orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL ?
                        android.widget.LinearLayout.HORIZONTAL : android.widget.LinearLayout.VERTICAL);
    }

    // Layout implementation

    public ViewGroup getLayoutManager() {
        return layoutManager;
    }

    public void add(AndroidViewComponent component) {
        layoutManager.addView(component.getView(), new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0f));
    }

    public void setHorizontalGravity(int gravity) {
        layoutManager.setHorizontalGravity(gravity);
    }

    public void setVerticalGravity(int gravity) {
        layoutManager.setVerticalGravity(gravity);
    }

    public void setBaselineAligned(boolean baselineAligned) { layoutManager.setBaselineAligned(baselineAligned); }
}
