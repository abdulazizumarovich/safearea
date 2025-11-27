package abdulaziz.umarovich.safearea;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * SafeArea provides a simple API for handling system insets when implementing edge-to-edge
 * either programmatically using {@link SafeArea#apply(View)} or wrapping a view by {@link SafeAreaFrame}
 * which extends {@link android.widget.FrameLayout}. 
 * It ensures initial (user defined) insets (margin or padding) of a view is preserved and extended 
 * when they are behind of a system bars & display cutouts & keyboards, so the view remains visible.
 */
public class SafeArea {

    public static final int EDGE_NONE = 0x00;
    public static final int EDGE_LEFT = 0x01; // 00000001
    public static final int EDGE_TOP = 0x02; // 00000010
    public static final int EDGE_RIGHT = 0x04; // 00000100
    public static final int EDGE_BOTTOM = 0x08; // 00001000

    public static final int EDGE_HORIZONTAL = EDGE_LEFT | EDGE_RIGHT;  // 0x05 - 00000101
    public static final int EDGE_VERTICAL = EDGE_TOP | EDGE_BOTTOM;  // 0x0A - 00001010
    public static final int EDGE_ALL = EDGE_HORIZONTAL | EDGE_VERTICAL; // 0x0F - 00001111

    /**
     * Inset type to extend when handling system insets.
     */
    public enum InsetType {
        PADDING,
        MARGIN
    }

    /**
     * Inset settings for a view to preserve initial insets to extend when system insets overlap.
     */
    public static class InsetSettings {
        private final Insets baseInset;
        private final int edges;
        private final InsetType type;

        /**
         * Creates InsetSettings for a given view.
         *
         * @param view  The view to which the insets will be applied.
         * @param edges A bitmask of edges to apply insets to.
         * @param type  The type of inset to modify (padding or margin).
         */
        public InsetSettings(@NonNull View view, int edges, InsetType type) {
            this.edges = edges;
            this.type = type;
            this.baseInset = initBaseInset(view, type);
        }

        /**
         * Initializes the base insets by capturing the initial padding or margin of the view.
         *
         * @param view  The view to initialize the insets for.
         * @param type  The type of inset to initialize (padding or margin).
         * @return The initialized {@link Insets} object.
         */
        private Insets initBaseInset(@NonNull View view, InsetType type) {
            if (type == InsetType.PADDING) {
                return Insets.of(
                        view.getPaddingLeft(),
                        view.getPaddingTop(),
                        view.getPaddingRight(),
                        view.getPaddingBottom()
                );
            }

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            return Insets.of(
                    lp.leftMargin,
                    lp.topMargin,
                    lp.rightMargin,
                    lp.bottomMargin
            );
        }

        /**
         * Checks if a specific edge is included in the settings.
         *
         * @param edge The edge to check.
         * @return true if the edge is included, false otherwise.
         */
        public boolean has(int edge) {
            if (edge == EDGE_NONE)
                return edges == EDGE_NONE;
            return (edges & edge) == edge;
        }

        /**
         * Computes the new insets by adding system insets to the base insets for the specified edges.
         *
         * @param inset The system insets to apply.
         * @param isLTR Whether the view is in left-to-right (LTR) layout.
         * @return The computed {@link Insets}.
         */
        private Insets computeOverlaps(Insets inset, boolean isLTR) {
            if (Insets.NONE.equals(inset))
                return Insets.NONE;

            int left = has(SafeArea.EDGE_LEFT) ? inset.left : 0;
            int top = has(SafeArea.EDGE_TOP) ? inset.top : 0;
            int right = has(SafeArea.EDGE_RIGHT) ? inset.right : 0;
            int bottom = has(SafeArea.EDGE_BOTTOM) ? inset.bottom : 0;

            // Handle RTL layout: swap left/right edges when only one horizontal edge is specified
            if (!isLTR && (edges & EDGE_HORIZONTAL) != EDGE_HORIZONTAL) {
                left = has(SafeArea.EDGE_RIGHT) ? inset.left : 0;
                right = has(SafeArea.EDGE_LEFT) ? inset.right : 0;
            }

            return Insets.of(baseInset.left + left,
                    baseInset.top + top,
                    baseInset.right + right,
                    baseInset.bottom + bottom);
        }

        /**
         * Updates the view's padding or margin with the computed insets.
         *
         * @param view  The view to update.
         * @param inset The computed insets to apply.
         */
        public void updateInsets(@NonNull View view, Insets inset) {
            boolean isLtr = view.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
            Insets computed = computeOverlaps(inset, isLtr);
            if (type == InsetType.PADDING) {
                view.setPadding(computed.left, computed.top, computed.right, computed.bottom);
            } else {
                ViewGroup.MarginLayoutParams lp =
                        (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                lp.leftMargin = computed.left;
                lp.topMargin = computed.top;
                lp.rightMargin = computed.right;
                lp.bottomMargin = computed.bottom;
                view.setLayoutParams(lp);
                if (Build.VERSION.SDK_INT < 26) {
                    view.getParent().requestLayout();
                }
            }
        }
    }

    /**
     * Applies safe area insets to all edges of the view using padding.
     *
     * @param view The view to which insets will be applied.
     * @see #apply(View, int)
     * @see #apply(View, InsetType)
     * @see #apply(View, int, InsetType)
     */
    public static void apply(@NonNull View view) {
        apply(view, SafeArea.EDGE_ALL, InsetType.PADDING);
    }

    /**
     * Applies safe area insets to all edges of the view using the specified inset type.
     *
     * @param view The view to which insets will be applied.
     * @param type The type of inset (padding or margin).
     * @see #apply(View)
     * @see #apply(View, int)
     * @see #apply(View, int, InsetType)
     */
    public static void apply(@NonNull View view, @NonNull InsetType type) {
        apply(view, SafeArea.EDGE_ALL, type);
    }

    /**
     * Applies safe area insets to the specified edges of the view using padding.
     *
     * @param view The view to which insets will be applied.
     * @param edge A bitmask of edges (e.g., {@link #EDGE_TOP} | {@link #EDGE_BOTTOM}).
     * @see #apply(View)
     * @see #apply(View, InsetType)
     * @see #apply(View, int, InsetType)
     */
    public static void apply(@NonNull View view, int edge) {
        apply(view, edge, InsetType.PADDING);
    }

    /**
     * Applies safe area insets to the specified edges of the view using the specified inset type.
     * This is the main method that sets up the window insets listener.
     *
     * @param view      The view to which insets will be applied.
     * @param edge      A bitmask of edges (e.g., {@link #EDGE_TOP} | {@link #EDGE_BOTTOM}).
     * @param insetType The type of inset (padding or margin).
     * @see #apply(View)
     * @see #apply(View, int)
     * @see #apply(View, InsetType)
     */
    public static void apply(@NonNull View view, int edge, @NonNull InsetType insetType) {
        InsetSettings settings = new InsetSettings(view, edge, insetType);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            settings.updateInsets(v, inset);
            return WindowInsetsCompat.CONSUMED;
        });
        requestApplyInsetsWhenAttached(view);
    }

    /**
     * This method is a workaround for a bug on devices running Android versions older than R (API 30).
     * On these older versions, if a parent view consumes window insets, those insets are not always
     * dispatched to its children. This method sets an {@link androidx.core.view.OnApplyWindowInsetsListener}
     * on the provided {@link ViewGroup} to manually dispatch the insets to all its children, ensuring
     * that child views with their own inset handling (like those using {@link SafeArea}) receive them correctly.
     *
     * @param viewGroup The ViewGroup that needs to forcefully dispatch insets to its children.
     */
    public static void forceDispatchInsets(@NonNull ViewGroup viewGroup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(viewGroup, (v, insets) -> {
            if (!(v instanceof ViewGroup)) return WindowInsetsCompat.CONSUMED;

            boolean consumed = false;
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                WindowInsetsCompat childResult = ViewCompat.dispatchApplyWindowInsets(vg.getChildAt(i), insets);
                if (childResult.isConsumed()) {
                    consumed = true;
                }
            }

            if (consumed) return WindowInsetsCompat.CONSUMED;
            else return insets;
        });
    }

    /**
     * Ensures that {@link ViewCompat#requestApplyInsets(View)} is called for the view.
     * If the view is already attached to a window, it requests insets immediately.
     * If not, it adds a listener to request insets when the view is attached.
     * @param view The view that needs to have insets applied.
     */
    private static void requestApplyInsetsWhenAttached(@NonNull View view) {
        if (view.isAttachedToWindow()) {
            // We're already attached, just request as normal.
            ViewCompat.requestApplyInsets(view);
            return;
        }

        // We're not attached to the hierarchy, add a listener to request when we are.
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                ViewCompat.requestApplyInsets(v);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                // no-op
            }
        });
    }
}
