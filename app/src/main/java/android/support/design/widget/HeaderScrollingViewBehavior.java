package android.support.design.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.math.MathUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import com.google.common.primitives.Ints;
import java.util.List;

abstract class HeaderScrollingViewBehavior extends ViewOffsetBehavior<View> {
    private int mOverlayTop;
    final Rect mTempRect1 = new Rect();
    final Rect mTempRect2 = new Rect();
    private int mVerticalLayoutGap = 0;

    /* access modifiers changed from: package-private */
    public abstract View findFirstDependency(List<View> list);

    public HeaderScrollingViewBehavior() {
    }

    public HeaderScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        View header;
        int i;
        int childLpHeight = child.getLayoutParams().height;
        if ((childLpHeight != -1 && childLpHeight != -2) || (header = findFirstDependency(parent.getDependencies(child))) == null) {
            return false;
        }
        if (ViewCompat.getFitsSystemWindows(header) && (!ViewCompat.getFitsSystemWindows(child))) {
            ViewCompat.setFitsSystemWindows(child, true);
            if (ViewCompat.getFitsSystemWindows(child)) {
                child.requestLayout();
                return true;
            }
        }
        int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
        if (availableHeight == 0) {
            availableHeight = parent.getHeight();
        }
        int height = (availableHeight - header.getMeasuredHeight()) + getScrollRange(header);
        if (childLpHeight == -1) {
            i = Ints.MAX_POWER_OF_TWO;
        } else {
            i = Integer.MIN_VALUE;
        }
        parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, View.MeasureSpec.makeMeasureSpec(height, i), heightUsed);
        return true;
    }

    /* access modifiers changed from: protected */
    public void layoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        View header = findFirstDependency(parent.getDependencies(child));
        if (header != null) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            Rect available = this.mTempRect1;
            available.set(parent.getPaddingLeft() + lp.leftMargin, header.getBottom() + lp.topMargin, (parent.getWidth() - parent.getPaddingRight()) - lp.rightMargin, ((parent.getHeight() + header.getBottom()) - parent.getPaddingBottom()) - lp.bottomMargin);
            WindowInsetsCompat parentInsets = parent.getLastWindowInsets();
            if (parentInsets != null && ViewCompat.getFitsSystemWindows(parent) && (!ViewCompat.getFitsSystemWindows(child))) {
                available.left += parentInsets.getSystemWindowInsetLeft();
                available.right -= parentInsets.getSystemWindowInsetRight();
            }
            Rect out = this.mTempRect2;
            GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(), child.getMeasuredHeight(), available, out, layoutDirection);
            int overlap = getOverlapPixelsForOffset(header);
            child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap);
            this.mVerticalLayoutGap = out.top - header.getBottom();
            return;
        }
        super.layoutChild(parent, child, layoutDirection);
        this.mVerticalLayoutGap = 0;
    }

    /* access modifiers changed from: package-private */
    public float getOverlapRatioForOffset(View header) {
        return 1.0f;
    }

    /* access modifiers changed from: package-private */
    public final int getOverlapPixelsForOffset(View header) {
        if (this.mOverlayTop == 0) {
            return 0;
        }
        return MathUtils.clamp((int) (getOverlapRatioForOffset(header) * ((float) this.mOverlayTop)), 0, this.mOverlayTop);
    }

    private static int resolveGravity(int gravity) {
        if (gravity == 0) {
            return 8388659;
        }
        return gravity;
    }

    /* access modifiers changed from: package-private */
    public int getScrollRange(View v) {
        return v.getMeasuredHeight();
    }

    /* access modifiers changed from: package-private */
    public final int getVerticalLayoutGap() {
        return this.mVerticalLayoutGap;
    }

    public final void setOverlayTop(int overlayTop) {
        this.mOverlayTop = overlayTop;
    }

    public final int getOverlayTop() {
        return this.mOverlayTop;
    }
}
