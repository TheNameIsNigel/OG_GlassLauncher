package android.support.constraint.solver.widgets;

import android.support.constraint.solver.ArrayRow;
import android.support.constraint.solver.LinearSystem;
import android.support.constraint.solver.SolverVariable;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.constraint.solver.widgets.ConstraintWidget;
import java.util.ArrayList;
import java.util.Arrays;

public class ConstraintWidgetContainer extends WidgetContainer {
    static boolean ALLOW_ROOT_GROUP = USE_SNAPSHOT;
    private static final int CHAIN_FIRST = 0;
    private static final int CHAIN_FIRST_VISIBLE = 2;
    private static final int CHAIN_LAST = 1;
    private static final int CHAIN_LAST_VISIBLE = 3;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_LAYOUT = false;
    private static final boolean DEBUG_OPTIMIZE = false;
    private static final int FLAG_CHAIN_DANGLING = 1;
    private static final int FLAG_RECOMPUTE_BOUNDS = 2;
    private static final int FLAG_USE_OPTIMIZE = 0;
    private static final int MAX_ITERATIONS = 8;
    public static final int OPTIMIZATION_ALL = 2;
    public static final int OPTIMIZATION_BASIC = 4;
    public static final int OPTIMIZATION_CHAIN = 8;
    public static final int OPTIMIZATION_NONE = 1;
    private static final boolean USE_SNAPSHOT = true;
    private static final boolean USE_THREAD = false;
    private boolean[] flags = new boolean[3];
    protected LinearSystem mBackgroundSystem = null;
    private ConstraintWidget[] mChainEnds = new ConstraintWidget[4];
    private boolean mHeightMeasuredTooSmall = false;
    private ConstraintWidget[] mHorizontalChainsArray = new ConstraintWidget[4];
    private int mHorizontalChainsSize = 0;
    private ConstraintWidget[] mMatchConstraintsChainedWidgets = new ConstraintWidget[4];
    private int mOptimizationLevel = 2;
    int mPaddingBottom;
    int mPaddingLeft;
    int mPaddingRight;
    int mPaddingTop;
    private Snapshot mSnapshot;
    protected LinearSystem mSystem = new LinearSystem();
    private ConstraintWidget[] mVerticalChainsArray = new ConstraintWidget[4];
    private int mVerticalChainsSize = 0;
    private boolean mWidthMeasuredTooSmall = false;
    int mWrapHeight;
    int mWrapWidth;

    public ConstraintWidgetContainer() {
    }

    public ConstraintWidgetContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public ConstraintWidgetContainer(int width, int height) {
        super(width, height);
    }

    public void setOptimizationLevel(int value) {
        this.mOptimizationLevel = value;
    }

    public String getType() {
        return "ConstraintLayout";
    }

    public void reset() {
        this.mSystem.reset();
        this.mPaddingLeft = 0;
        this.mPaddingRight = 0;
        this.mPaddingTop = 0;
        this.mPaddingBottom = 0;
        super.reset();
    }

    public boolean isWidthMeasuredTooSmall() {
        return this.mWidthMeasuredTooSmall;
    }

    public boolean isHeightMeasuredTooSmall() {
        return this.mHeightMeasuredTooSmall;
    }

    public static ConstraintWidgetContainer createContainer(ConstraintWidgetContainer container, String name, ArrayList<ConstraintWidget> widgets, int padding) {
        Rectangle bounds = getBounds(widgets);
        if (bounds.width == 0 || bounds.height == 0) {
            return null;
        }
        if (padding > 0) {
            int maxPadding = Math.min(bounds.x, bounds.y);
            if (padding > maxPadding) {
                padding = maxPadding;
            }
            bounds.grow(padding, padding);
        }
        container.setOrigin(bounds.x, bounds.y);
        container.setDimension(bounds.width, bounds.height);
        container.setDebugName(name);
        ConstraintWidget parent = widgets.get(0).getParent();
        int widgetsSize = widgets.size();
        for (int i = 0; i < widgetsSize; i++) {
            ConstraintWidget widget = widgets.get(i);
            if (widget.getParent() == parent) {
                container.add(widget);
                widget.setX(widget.getX() - bounds.x);
                widget.setY(widget.getY() - bounds.y);
            }
        }
        return container;
    }

    public boolean addChildrenToSolver(LinearSystem system, int group) {
        addToSolver(system, group);
        int count = this.mChildren.size();
        boolean setMatchParent = false;
        if (this.mOptimizationLevel != 2 && this.mOptimizationLevel != 4) {
            setMatchParent = USE_SNAPSHOT;
        } else if (optimize(system)) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(i);
            if (!(widget instanceof ConstraintWidgetContainer)) {
                if (setMatchParent) {
                    Optimizer.checkMatchParent(this, system, widget);
                }
                widget.addToSolver(system, group);
            } else {
                ConstraintWidget.DimensionBehaviour horizontalBehaviour = widget.mHorizontalDimensionBehaviour;
                ConstraintWidget.DimensionBehaviour verticalBehaviour = widget.mVerticalDimensionBehaviour;
                if (horizontalBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    widget.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED);
                }
                if (verticalBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    widget.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED);
                }
                widget.addToSolver(system, group);
                if (horizontalBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    widget.setHorizontalDimensionBehaviour(horizontalBehaviour);
                }
                if (verticalBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    widget.setVerticalDimensionBehaviour(verticalBehaviour);
                }
            }
        }
        if (this.mHorizontalChainsSize > 0) {
            applyHorizontalChain(system);
        }
        if (this.mVerticalChainsSize <= 0) {
            return USE_SNAPSHOT;
        }
        applyVerticalChain(system);
        return USE_SNAPSHOT;
    }

    private boolean optimize(LinearSystem system) {
        int count = this.mChildren.size();
        boolean done = false;
        int dv = 0;
        int dh = 0;
        int n = 0;
        for (int i = 0; i < count; i++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(i);
            widget.mHorizontalResolution = -1;
            widget.mVerticalResolution = -1;
            if (widget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || widget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                widget.mHorizontalResolution = 1;
                widget.mVerticalResolution = 1;
            }
            if (widget instanceof Barrier) {
                widget.mHorizontalResolution = 1;
                widget.mVerticalResolution = 1;
            }
        }
        while (!done) {
            int prev = dv;
            int preh = dh;
            dv = 0;
            dh = 0;
            n++;
            for (int i2 = 0; i2 < count; i2++) {
                ConstraintWidget widget2 = (ConstraintWidget) this.mChildren.get(i2);
                if (widget2.mHorizontalResolution == -1) {
                    if (this.mHorizontalDimensionBehaviour != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                        Optimizer.checkHorizontalSimpleDependency(this, system, widget2);
                    } else {
                        widget2.mHorizontalResolution = 1;
                    }
                }
                if (widget2.mVerticalResolution == -1) {
                    if (this.mVerticalDimensionBehaviour != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                        Optimizer.checkVerticalSimpleDependency(this, system, widget2);
                    } else {
                        widget2.mVerticalResolution = 1;
                    }
                }
                if (widget2.mVerticalResolution == -1) {
                    dv++;
                }
                if (widget2.mHorizontalResolution == -1) {
                    dh++;
                }
            }
            if (dv == 0 && dh == 0) {
                done = USE_SNAPSHOT;
            } else if (prev == dv && preh == dh) {
                done = USE_SNAPSHOT;
            }
        }
        int sh = 0;
        int sv = 0;
        for (int i3 = 0; i3 < count; i3++) {
            ConstraintWidget widget3 = (ConstraintWidget) this.mChildren.get(i3);
            if (widget3.mHorizontalResolution == 1 || widget3.mHorizontalResolution == -1) {
                sh++;
            }
            if (widget3.mVerticalResolution == 1 || widget3.mVerticalResolution == -1) {
                sv++;
            }
        }
        if (sh == 0 && sv == 0) {
            return USE_SNAPSHOT;
        }
        return false;
    }

    private void applyHorizontalChain(LinearSystem system) {
        SolverVariable rightTarget;
        for (int i = 0; i < this.mHorizontalChainsSize; i++) {
            ConstraintWidget first = this.mHorizontalChainsArray[i];
            int numMatchConstraints = countMatchConstraintsChainedWidgets(system, this.mChainEnds, this.mHorizontalChainsArray[i], 0, this.flags);
            ConstraintWidget currentWidget = this.mChainEnds[2];
            if (currentWidget != null) {
                if (!this.flags[1]) {
                    boolean isChainSpread = first.mHorizontalChainStyle != 0 ? false : USE_SNAPSHOT;
                    boolean isChainPacked = first.mHorizontalChainStyle != 2 ? false : USE_SNAPSHOT;
                    ConstraintWidget constraintWidget = first;
                    boolean isWrapContent = this.mHorizontalDimensionBehaviour != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT ? false : USE_SNAPSHOT;
                    if ((this.mOptimizationLevel == 2 || this.mOptimizationLevel == 8) && this.flags[0] && first.mHorizontalChainFixedPosition && !isChainPacked && !isWrapContent && first.mHorizontalChainStyle == 0) {
                        Optimizer.applyDirectResolutionHorizontalChain(this, system, numMatchConstraints, first);
                    } else if (numMatchConstraints != 0 && !isChainPacked) {
                        ConstraintWidget previous = null;
                        float totalWeights = 0.0f;
                        while (currentWidget != null) {
                            if (currentWidget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                totalWeights += currentWidget.mHorizontalWeight;
                                int margin = 0;
                                if (currentWidget.mRight.mTarget != null) {
                                    margin = currentWidget.mRight.getMargin();
                                    if (currentWidget != this.mChainEnds[3]) {
                                        margin += currentWidget.mRight.mTarget.mOwner.mLeft.getMargin();
                                    }
                                }
                                system.addGreaterThan(currentWidget.mRight.mSolverVariable, currentWidget.mLeft.mSolverVariable, 0, 1);
                                system.addLowerThan(currentWidget.mRight.mSolverVariable, currentWidget.mRight.mTarget.mSolverVariable, -margin, 1);
                            } else {
                                int margin2 = currentWidget.mLeft.getMargin();
                                if (previous != null) {
                                    margin2 += previous.mRight.getMargin();
                                }
                                int strength = 3;
                                if (currentWidget.mLeft.mTarget.mOwner.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                    strength = 2;
                                }
                                system.addGreaterThan(currentWidget.mLeft.mSolverVariable, currentWidget.mLeft.mTarget.mSolverVariable, margin2, strength);
                                int margin3 = currentWidget.mRight.getMargin();
                                if (currentWidget.mRight.mTarget.mOwner.mLeft.mTarget != null && currentWidget.mRight.mTarget.mOwner.mLeft.mTarget.mOwner == currentWidget) {
                                    margin3 += currentWidget.mRight.mTarget.mOwner.mLeft.getMargin();
                                }
                                int strength2 = 3;
                                if (currentWidget.mRight.mTarget.mOwner.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                    strength2 = 2;
                                }
                                SolverVariable rightTarget2 = currentWidget.mRight.mTarget.mSolverVariable;
                                if (currentWidget == this.mChainEnds[3]) {
                                    rightTarget2 = this.mChainEnds[1].mRight.mTarget.mSolverVariable;
                                    strength2 = 3;
                                }
                                system.addLowerThan(currentWidget.mRight.mSolverVariable, rightTarget2, -margin3, strength2);
                            }
                            previous = currentWidget;
                            currentWidget = currentWidget.mHorizontalNextWidget;
                        }
                        if (numMatchConstraints != 1) {
                            for (int j = 0; j < numMatchConstraints - 1; j++) {
                                ConstraintWidget current = this.mMatchConstraintsChainedWidgets[j];
                                ConstraintWidget nextWidget = this.mMatchConstraintsChainedWidgets[j + 1];
                                SolverVariable left = current.mLeft.mSolverVariable;
                                SolverVariable right = current.mRight.mSolverVariable;
                                SolverVariable nextLeft = nextWidget.mLeft.mSolverVariable;
                                SolverVariable nextRight = nextWidget.mRight.mSolverVariable;
                                if (nextWidget == this.mChainEnds[3]) {
                                    nextRight = this.mChainEnds[1].mRight.mSolverVariable;
                                }
                                int margin4 = current.mLeft.getMargin();
                                if (!(current.mLeft.mTarget == null || current.mLeft.mTarget.mOwner.mRight.mTarget == null || current.mLeft.mTarget.mOwner.mRight.mTarget.mOwner != current)) {
                                    margin4 += current.mLeft.mTarget.mOwner.mRight.getMargin();
                                }
                                system.addGreaterThan(left, current.mLeft.mTarget.mSolverVariable, margin4, 2);
                                int margin5 = current.mRight.getMargin();
                                if (!(current.mRight.mTarget == null || current.mHorizontalNextWidget == null)) {
                                    margin5 += current.mHorizontalNextWidget.mLeft.mTarget == null ? 0 : current.mHorizontalNextWidget.mLeft.getMargin();
                                }
                                system.addLowerThan(right, current.mRight.mTarget.mSolverVariable, -margin5, 2);
                                if (j + 1 == numMatchConstraints - 1) {
                                    int margin6 = nextWidget.mLeft.getMargin();
                                    if (!(nextWidget.mLeft.mTarget == null || nextWidget.mLeft.mTarget.mOwner.mRight.mTarget == null || nextWidget.mLeft.mTarget.mOwner.mRight.mTarget.mOwner != nextWidget)) {
                                        margin6 += nextWidget.mLeft.mTarget.mOwner.mRight.getMargin();
                                    }
                                    system.addGreaterThan(nextLeft, nextWidget.mLeft.mTarget.mSolverVariable, margin6, 2);
                                    ConstraintAnchor anchor = nextWidget.mRight;
                                    if (nextWidget == this.mChainEnds[3]) {
                                        anchor = this.mChainEnds[1].mRight;
                                    }
                                    int margin7 = anchor.getMargin();
                                    if (!(anchor.mTarget == null || anchor.mTarget.mOwner.mLeft.mTarget == null || anchor.mTarget.mOwner.mLeft.mTarget.mOwner != nextWidget)) {
                                        margin7 += anchor.mTarget.mOwner.mLeft.getMargin();
                                    }
                                    system.addLowerThan(nextRight, anchor.mTarget.mSolverVariable, -margin7, 2);
                                }
                                if (first.mMatchConstraintMaxWidth > 0) {
                                    system.addLowerThan(right, left, first.mMatchConstraintMaxWidth, 2);
                                }
                                ArrayRow row = system.createRow();
                                row.createRowEqualDimension(current.mHorizontalWeight, totalWeights, nextWidget.mHorizontalWeight, left, current.mLeft.getMargin(), right, current.mRight.getMargin(), nextLeft, nextWidget.mLeft.getMargin(), nextRight, nextWidget.mRight.getMargin());
                                system.addConstraint(row);
                            }
                        } else {
                            ConstraintWidget w = this.mMatchConstraintsChainedWidgets[0];
                            int leftMargin = w.mLeft.getMargin();
                            if (w.mLeft.mTarget != null) {
                                leftMargin += w.mLeft.mTarget.getMargin();
                            }
                            int rightMargin = w.mRight.getMargin();
                            if (w.mRight.mTarget != null) {
                                rightMargin += w.mRight.mTarget.getMargin();
                            }
                            SolverVariable rightTarget3 = first.mRight.mTarget.mSolverVariable;
                            if (w == this.mChainEnds[3]) {
                                rightTarget3 = this.mChainEnds[1].mRight.mTarget.mSolverVariable;
                            }
                            if (w.mMatchConstraintDefaultWidth != 1) {
                                system.addEquality(w.mLeft.mSolverVariable, w.mLeft.mTarget.mSolverVariable, leftMargin, 1);
                                system.addEquality(w.mRight.mSolverVariable, rightTarget3, -rightMargin, 1);
                            } else {
                                system.addGreaterThan(first.mLeft.mSolverVariable, first.mLeft.mTarget.mSolverVariable, leftMargin, 1);
                                system.addLowerThan(first.mRight.mSolverVariable, rightTarget3, -rightMargin, 1);
                                system.addEquality(first.mRight.mSolverVariable, first.mLeft.mSolverVariable, first.getWidth(), 2);
                            }
                        }
                    } else {
                        ConstraintWidget previousVisibleWidget = null;
                        ConstraintWidget lastWidget = null;
                        ConstraintWidget firstVisibleWidget = currentWidget;
                        boolean isLast = false;
                        while (currentWidget != null) {
                            ConstraintWidget next = currentWidget.mHorizontalNextWidget;
                            if (next == null) {
                                lastWidget = this.mChainEnds[1];
                                isLast = USE_SNAPSHOT;
                            }
                            if (isChainPacked) {
                                ConstraintAnchor left2 = currentWidget.mLeft;
                                int margin8 = left2.getMargin();
                                if (previousVisibleWidget != null) {
                                    margin8 += previousVisibleWidget.mRight.getMargin();
                                }
                                int strength3 = 1;
                                if (firstVisibleWidget != currentWidget) {
                                    strength3 = 3;
                                }
                                system.addGreaterThan(left2.mSolverVariable, left2.mTarget.mSolverVariable, margin8, strength3);
                                if (currentWidget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                    ConstraintAnchor right2 = currentWidget.mRight;
                                    if (currentWidget.mMatchConstraintDefaultWidth != 1) {
                                        system.addGreaterThan(left2.mSolverVariable, left2.mTarget.mSolverVariable, left2.mMargin, 3);
                                        system.addLowerThan(right2.mSolverVariable, left2.mSolverVariable, currentWidget.mMatchConstraintMinWidth, 3);
                                    } else {
                                        system.addEquality(right2.mSolverVariable, left2.mSolverVariable, Math.max(currentWidget.mMatchConstraintMinWidth, currentWidget.getWidth()), 3);
                                    }
                                }
                            } else if (!isChainSpread && isLast && previousVisibleWidget != null) {
                                if (currentWidget.mRight.mTarget != null) {
                                    system.addEquality(currentWidget.mRight.mSolverVariable, lastWidget.mRight.mTarget.mSolverVariable, -currentWidget.mRight.getMargin(), 5);
                                } else {
                                    system.addEquality(currentWidget.mRight.mSolverVariable, currentWidget.getDrawRight());
                                }
                            } else if (isChainSpread || isLast || previousVisibleWidget != null) {
                                ConstraintAnchor left3 = currentWidget.mLeft;
                                ConstraintAnchor right3 = currentWidget.mRight;
                                int leftMargin2 = left3.getMargin();
                                int rightMargin2 = right3.getMargin();
                                system.addGreaterThan(left3.mSolverVariable, left3.mTarget.mSolverVariable, leftMargin2, 1);
                                system.addLowerThan(right3.mSolverVariable, right3.mTarget.mSolverVariable, -rightMargin2, 1);
                                SolverVariable leftTarget = left3.mTarget == null ? null : left3.mTarget.mSolverVariable;
                                if (previousVisibleWidget == null) {
                                    leftTarget = first.mLeft.mTarget == null ? null : first.mLeft.mTarget.mSolverVariable;
                                }
                                if (next == null) {
                                    next = lastWidget.mRight.mTarget == null ? null : lastWidget.mRight.mTarget.mOwner;
                                }
                                if (next != null) {
                                    SolverVariable rightTarget4 = next.mLeft.mSolverVariable;
                                    if (isLast) {
                                        rightTarget4 = lastWidget.mRight.mTarget == null ? null : lastWidget.mRight.mTarget.mSolverVariable;
                                    }
                                    if (!(leftTarget == null || rightTarget4 == null)) {
                                        system.addCentering(left3.mSolverVariable, leftTarget, leftMargin2, 0.5f, rightTarget4, right3.mSolverVariable, rightMargin2, 4);
                                    }
                                }
                            } else if (currentWidget.mLeft.mTarget != null) {
                                system.addEquality(currentWidget.mLeft.mSolverVariable, first.mLeft.mTarget.mSolverVariable, currentWidget.mLeft.getMargin(), 5);
                            } else {
                                system.addEquality(currentWidget.mLeft.mSolverVariable, currentWidget.getDrawX());
                            }
                            previousVisibleWidget = currentWidget;
                            if (!isLast) {
                                currentWidget = next;
                            } else {
                                currentWidget = null;
                            }
                        }
                        if (isChainPacked) {
                            ConstraintAnchor left4 = firstVisibleWidget.mLeft;
                            ConstraintAnchor right4 = lastWidget.mRight;
                            int leftMargin3 = left4.getMargin();
                            int rightMargin3 = right4.getMargin();
                            SolverVariable leftTarget2 = first.mLeft.mTarget == null ? null : first.mLeft.mTarget.mSolverVariable;
                            if (lastWidget.mRight.mTarget == null) {
                                rightTarget = null;
                            } else {
                                rightTarget = lastWidget.mRight.mTarget.mSolverVariable;
                            }
                            if (!(leftTarget2 == null || rightTarget == null)) {
                                system.addLowerThan(right4.mSolverVariable, rightTarget, -rightMargin3, 1);
                                system.addCentering(left4.mSolverVariable, leftTarget2, leftMargin3, first.mHorizontalBiasPercent, rightTarget, right4.mSolverVariable, rightMargin3, 4);
                            }
                        }
                    }
                } else {
                    int x = first.getDrawX();
                    while (currentWidget != null) {
                        system.addEquality(currentWidget.mLeft.mSolverVariable, x);
                        ConstraintWidget next2 = currentWidget.mHorizontalNextWidget;
                        x += currentWidget.mLeft.getMargin() + currentWidget.getWidth() + currentWidget.mRight.getMargin();
                        currentWidget = next2;
                    }
                }
            }
        }
    }

    private void applyVerticalChain(LinearSystem system) {
        SolverVariable bottomTarget;
        for (int i = 0; i < this.mVerticalChainsSize; i++) {
            ConstraintWidget first = this.mVerticalChainsArray[i];
            int numMatchConstraints = countMatchConstraintsChainedWidgets(system, this.mChainEnds, this.mVerticalChainsArray[i], 1, this.flags);
            ConstraintWidget currentWidget = this.mChainEnds[2];
            if (currentWidget != null) {
                if (!this.flags[1]) {
                    boolean isChainSpread = first.mVerticalChainStyle != 0 ? false : USE_SNAPSHOT;
                    boolean isChainPacked = first.mVerticalChainStyle != 2 ? false : USE_SNAPSHOT;
                    ConstraintWidget constraintWidget = first;
                    boolean isWrapContent = this.mVerticalDimensionBehaviour != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT ? false : USE_SNAPSHOT;
                    if ((this.mOptimizationLevel == 2 || this.mOptimizationLevel == 8) && this.flags[0] && first.mVerticalChainFixedPosition && !isChainPacked && !isWrapContent && first.mVerticalChainStyle == 0) {
                        Optimizer.applyDirectResolutionVerticalChain(this, system, numMatchConstraints, first);
                    } else if (numMatchConstraints != 0 && !isChainPacked) {
                        ConstraintWidget previous = null;
                        float totalWeights = 0.0f;
                        while (currentWidget != null) {
                            if (currentWidget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                totalWeights += currentWidget.mVerticalWeight;
                                int margin = 0;
                                if (currentWidget.mBottom.mTarget != null) {
                                    margin = currentWidget.mBottom.getMargin();
                                    if (currentWidget != this.mChainEnds[3]) {
                                        margin += currentWidget.mBottom.mTarget.mOwner.mTop.getMargin();
                                    }
                                }
                                system.addGreaterThan(currentWidget.mBottom.mSolverVariable, currentWidget.mTop.mSolverVariable, 0, 1);
                                system.addLowerThan(currentWidget.mBottom.mSolverVariable, currentWidget.mBottom.mTarget.mSolverVariable, -margin, 1);
                            } else {
                                int margin2 = currentWidget.mTop.getMargin();
                                if (previous != null) {
                                    margin2 += previous.mBottom.getMargin();
                                }
                                int strength = 3;
                                if (currentWidget.mTop.mTarget.mOwner.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                    strength = 2;
                                }
                                system.addGreaterThan(currentWidget.mTop.mSolverVariable, currentWidget.mTop.mTarget.mSolverVariable, margin2, strength);
                                int margin3 = currentWidget.mBottom.getMargin();
                                if (currentWidget.mBottom.mTarget.mOwner.mTop.mTarget != null && currentWidget.mBottom.mTarget.mOwner.mTop.mTarget.mOwner == currentWidget) {
                                    margin3 += currentWidget.mBottom.mTarget.mOwner.mTop.getMargin();
                                }
                                int strength2 = 3;
                                if (currentWidget.mBottom.mTarget.mOwner.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                    strength2 = 2;
                                }
                                SolverVariable bottomTarget2 = currentWidget.mBottom.mTarget.mSolverVariable;
                                if (currentWidget == this.mChainEnds[3]) {
                                    bottomTarget2 = this.mChainEnds[1].mBottom.mTarget.mSolverVariable;
                                    strength2 = 3;
                                }
                                system.addLowerThan(currentWidget.mBottom.mSolverVariable, bottomTarget2, -margin3, strength2);
                            }
                            previous = currentWidget;
                            currentWidget = currentWidget.mVerticalNextWidget;
                        }
                        if (numMatchConstraints != 1) {
                            for (int j = 0; j < numMatchConstraints - 1; j++) {
                                ConstraintWidget current = this.mMatchConstraintsChainedWidgets[j];
                                ConstraintWidget nextWidget = this.mMatchConstraintsChainedWidgets[j + 1];
                                SolverVariable top = current.mTop.mSolverVariable;
                                SolverVariable bottom = current.mBottom.mSolverVariable;
                                SolverVariable nextTop = nextWidget.mTop.mSolverVariable;
                                SolverVariable nextBottom = nextWidget.mBottom.mSolverVariable;
                                if (nextWidget == this.mChainEnds[3]) {
                                    nextBottom = this.mChainEnds[1].mBottom.mSolverVariable;
                                }
                                int margin4 = current.mTop.getMargin();
                                if (!(current.mTop.mTarget == null || current.mTop.mTarget.mOwner.mBottom.mTarget == null || current.mTop.mTarget.mOwner.mBottom.mTarget.mOwner != current)) {
                                    margin4 += current.mTop.mTarget.mOwner.mBottom.getMargin();
                                }
                                system.addGreaterThan(top, current.mTop.mTarget.mSolverVariable, margin4, 2);
                                int margin5 = current.mBottom.getMargin();
                                if (!(current.mBottom.mTarget == null || current.mVerticalNextWidget == null)) {
                                    margin5 += current.mVerticalNextWidget.mTop.mTarget == null ? 0 : current.mVerticalNextWidget.mTop.getMargin();
                                }
                                system.addLowerThan(bottom, current.mBottom.mTarget.mSolverVariable, -margin5, 2);
                                if (j + 1 == numMatchConstraints - 1) {
                                    int margin6 = nextWidget.mTop.getMargin();
                                    if (!(nextWidget.mTop.mTarget == null || nextWidget.mTop.mTarget.mOwner.mBottom.mTarget == null || nextWidget.mTop.mTarget.mOwner.mBottom.mTarget.mOwner != nextWidget)) {
                                        margin6 += nextWidget.mTop.mTarget.mOwner.mBottom.getMargin();
                                    }
                                    system.addGreaterThan(nextTop, nextWidget.mTop.mTarget.mSolverVariable, margin6, 2);
                                    ConstraintAnchor anchor = nextWidget.mBottom;
                                    if (nextWidget == this.mChainEnds[3]) {
                                        anchor = this.mChainEnds[1].mBottom;
                                    }
                                    int margin7 = anchor.getMargin();
                                    if (!(anchor.mTarget == null || anchor.mTarget.mOwner.mTop.mTarget == null || anchor.mTarget.mOwner.mTop.mTarget.mOwner != nextWidget)) {
                                        margin7 += anchor.mTarget.mOwner.mTop.getMargin();
                                    }
                                    system.addLowerThan(nextBottom, anchor.mTarget.mSolverVariable, -margin7, 2);
                                }
                                if (first.mMatchConstraintMaxHeight > 0) {
                                    system.addLowerThan(bottom, top, first.mMatchConstraintMaxHeight, 2);
                                }
                                ArrayRow row = system.createRow();
                                row.createRowEqualDimension(current.mVerticalWeight, totalWeights, nextWidget.mVerticalWeight, top, current.mTop.getMargin(), bottom, current.mBottom.getMargin(), nextTop, nextWidget.mTop.getMargin(), nextBottom, nextWidget.mBottom.getMargin());
                                system.addConstraint(row);
                            }
                        } else {
                            ConstraintWidget w = this.mMatchConstraintsChainedWidgets[0];
                            int topMargin = w.mTop.getMargin();
                            if (w.mTop.mTarget != null) {
                                topMargin += w.mTop.mTarget.getMargin();
                            }
                            int bottomMargin = w.mBottom.getMargin();
                            if (w.mBottom.mTarget != null) {
                                bottomMargin += w.mBottom.mTarget.getMargin();
                            }
                            SolverVariable bottomTarget3 = first.mBottom.mTarget.mSolverVariable;
                            if (w == this.mChainEnds[3]) {
                                bottomTarget3 = this.mChainEnds[1].mBottom.mTarget.mSolverVariable;
                            }
                            if (w.mMatchConstraintDefaultHeight != 1) {
                                system.addEquality(w.mTop.mSolverVariable, w.mTop.mTarget.mSolverVariable, topMargin, 1);
                                system.addEquality(w.mBottom.mSolverVariable, bottomTarget3, -bottomMargin, 1);
                            } else {
                                system.addGreaterThan(first.mTop.mSolverVariable, first.mTop.mTarget.mSolverVariable, topMargin, 1);
                                system.addLowerThan(first.mBottom.mSolverVariable, bottomTarget3, -bottomMargin, 1);
                                system.addEquality(first.mBottom.mSolverVariable, first.mTop.mSolverVariable, first.getHeight(), 2);
                            }
                        }
                    } else {
                        ConstraintWidget previousVisibleWidget = null;
                        ConstraintWidget lastWidget = null;
                        ConstraintWidget firstVisibleWidget = currentWidget;
                        boolean isLast = false;
                        while (currentWidget != null) {
                            ConstraintWidget next = currentWidget.mVerticalNextWidget;
                            if (next == null) {
                                lastWidget = this.mChainEnds[1];
                                isLast = USE_SNAPSHOT;
                            }
                            if (isChainPacked) {
                                ConstraintAnchor top2 = currentWidget.mTop;
                                int margin8 = top2.getMargin();
                                if (previousVisibleWidget != null) {
                                    margin8 += previousVisibleWidget.mBottom.getMargin();
                                }
                                int strength3 = 1;
                                if (firstVisibleWidget != currentWidget) {
                                    strength3 = 3;
                                }
                                SolverVariable source = null;
                                SolverVariable target = null;
                                if (top2.mTarget != null) {
                                    source = top2.mSolverVariable;
                                    target = top2.mTarget.mSolverVariable;
                                } else if (currentWidget.mBaseline.mTarget != null) {
                                    source = currentWidget.mBaseline.mSolverVariable;
                                    target = currentWidget.mBaseline.mTarget.mSolverVariable;
                                    margin8 -= top2.getMargin();
                                }
                                if (!(source == null || target == null)) {
                                    system.addGreaterThan(source, target, margin8, strength3);
                                }
                                if (currentWidget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                                    ConstraintAnchor bottom2 = currentWidget.mBottom;
                                    if (currentWidget.mMatchConstraintDefaultHeight != 1) {
                                        system.addGreaterThan(top2.mSolverVariable, top2.mTarget.mSolverVariable, top2.mMargin, 3);
                                        system.addLowerThan(bottom2.mSolverVariable, top2.mSolverVariable, currentWidget.mMatchConstraintMinHeight, 3);
                                    } else {
                                        system.addEquality(bottom2.mSolverVariable, top2.mSolverVariable, Math.max(currentWidget.mMatchConstraintMinHeight, currentWidget.getHeight()), 3);
                                    }
                                }
                            } else if (!isChainSpread && isLast && previousVisibleWidget != null) {
                                if (currentWidget.mBottom.mTarget != null) {
                                    system.addEquality(currentWidget.mBottom.mSolverVariable, lastWidget.mBottom.mTarget.mSolverVariable, -currentWidget.mBottom.getMargin(), 5);
                                } else {
                                    system.addEquality(currentWidget.mBottom.mSolverVariable, currentWidget.getDrawBottom());
                                }
                            } else if (isChainSpread || isLast || previousVisibleWidget != null) {
                                ConstraintAnchor top3 = currentWidget.mTop;
                                ConstraintAnchor bottom3 = currentWidget.mBottom;
                                int topMargin2 = top3.getMargin();
                                int bottomMargin2 = bottom3.getMargin();
                                system.addGreaterThan(top3.mSolverVariable, top3.mTarget.mSolverVariable, topMargin2, 1);
                                system.addLowerThan(bottom3.mSolverVariable, bottom3.mTarget.mSolverVariable, -bottomMargin2, 1);
                                SolverVariable topTarget = top3.mTarget == null ? null : top3.mTarget.mSolverVariable;
                                if (previousVisibleWidget == null) {
                                    topTarget = first.mTop.mTarget == null ? null : first.mTop.mTarget.mSolverVariable;
                                }
                                if (next == null) {
                                    next = lastWidget.mBottom.mTarget == null ? null : lastWidget.mBottom.mTarget.mOwner;
                                }
                                if (next != null) {
                                    SolverVariable bottomTarget4 = next.mTop.mSolverVariable;
                                    if (isLast) {
                                        bottomTarget4 = lastWidget.mBottom.mTarget == null ? null : lastWidget.mBottom.mTarget.mSolverVariable;
                                    }
                                    if (!(topTarget == null || bottomTarget4 == null)) {
                                        system.addCentering(top3.mSolverVariable, topTarget, topMargin2, 0.5f, bottomTarget4, bottom3.mSolverVariable, bottomMargin2, 4);
                                    }
                                }
                            } else if (currentWidget.mTop.mTarget != null) {
                                system.addEquality(currentWidget.mTop.mSolverVariable, first.mTop.mTarget.mSolverVariable, currentWidget.mTop.getMargin(), 5);
                            } else {
                                system.addEquality(currentWidget.mTop.mSolverVariable, currentWidget.getDrawY());
                            }
                            previousVisibleWidget = currentWidget;
                            if (!isLast) {
                                currentWidget = next;
                            } else {
                                currentWidget = null;
                            }
                        }
                        if (isChainPacked) {
                            ConstraintAnchor top4 = firstVisibleWidget.mTop;
                            ConstraintAnchor bottom4 = lastWidget.mBottom;
                            int topMargin3 = top4.getMargin();
                            int bottomMargin3 = bottom4.getMargin();
                            SolverVariable topTarget2 = first.mTop.mTarget == null ? null : first.mTop.mTarget.mSolverVariable;
                            if (lastWidget.mBottom.mTarget == null) {
                                bottomTarget = null;
                            } else {
                                bottomTarget = lastWidget.mBottom.mTarget.mSolverVariable;
                            }
                            if (!(topTarget2 == null || bottomTarget == null)) {
                                system.addLowerThan(bottom4.mSolverVariable, bottomTarget, -bottomMargin3, 1);
                                system.addCentering(top4.mSolverVariable, topTarget2, topMargin3, first.mVerticalBiasPercent, bottomTarget, bottom4.mSolverVariable, bottomMargin3, 4);
                            }
                        }
                    }
                } else {
                    int y = first.getDrawY();
                    while (currentWidget != null) {
                        system.addEquality(currentWidget.mTop.mSolverVariable, y);
                        ConstraintWidget next2 = currentWidget.mVerticalNextWidget;
                        y += currentWidget.mTop.getMargin() + currentWidget.getHeight() + currentWidget.mBottom.getMargin();
                        currentWidget = next2;
                    }
                }
            }
        }
    }

    public void updateChildrenFromSolver(LinearSystem system, int group, boolean[] flags2) {
        flags2[2] = false;
        updateFromSolver(system, group);
        int count = this.mChildren.size();
        for (int i = 0; i < count; i++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(i);
            widget.updateFromSolver(system, group);
            if (widget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.getWidth() < widget.getWrapWidth()) {
                flags2[2] = USE_SNAPSHOT;
            }
            if (widget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.getHeight() < widget.getWrapHeight()) {
                flags2[2] = USE_SNAPSHOT;
            }
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        this.mPaddingLeft = left;
        this.mPaddingTop = top;
        this.mPaddingRight = right;
        this.mPaddingBottom = bottom;
    }

    public void layout() {
        int prex = this.mX;
        int prey = this.mY;
        int prew = Math.max(0, getWidth());
        int preh = Math.max(0, getHeight());
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
        if (this.mParent == null) {
            this.mX = 0;
            this.mY = 0;
        } else {
            if (this.mSnapshot == null) {
                this.mSnapshot = new Snapshot(this);
            }
            this.mSnapshot.updateFrom(this);
            setX(this.mPaddingLeft);
            setY(this.mPaddingTop);
            resetAnchors();
            resetSolverVariables(this.mSystem.getCache());
        }
        boolean wrap_override = false;
        ConstraintWidget.DimensionBehaviour originalVerticalDimensionBehaviour = this.mVerticalDimensionBehaviour;
        ConstraintWidget.DimensionBehaviour originalHorizontalDimensionBehaviour = this.mHorizontalDimensionBehaviour;
        if (this.mOptimizationLevel == 2 && (this.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT || this.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)) {
            findWrapSize(this.mChildren, this.flags);
            wrap_override = this.flags[0];
            if (prew > 0 && preh > 0 && (this.mWrapWidth > prew || this.mWrapHeight > preh)) {
                wrap_override = false;
            }
            if (wrap_override) {
                if (this.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    this.mHorizontalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED;
                    if (prew > 0 && prew < this.mWrapWidth) {
                        this.mWidthMeasuredTooSmall = USE_SNAPSHOT;
                        setWidth(prew);
                    } else {
                        setWidth(Math.max(this.mMinWidth, this.mWrapWidth));
                    }
                }
                if (this.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    this.mVerticalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED;
                    if (preh > 0 && preh < this.mWrapHeight) {
                        this.mHeightMeasuredTooSmall = USE_SNAPSHOT;
                        setHeight(preh);
                    } else {
                        setHeight(Math.max(this.mMinHeight, this.mWrapHeight));
                    }
                }
            }
        }
        resetChains();
        int count = this.mChildren.size();
        for (int i = 0; i < count; i++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(i);
            if (widget instanceof WidgetContainer) {
                ((WidgetContainer) widget).layout();
            }
        }
        boolean needsSolving = USE_SNAPSHOT;
        int countSolve = 0;
        while (needsSolving) {
            countSolve++;
            try {
                this.mSystem.reset();
                needsSolving = addChildrenToSolver(this.mSystem, Integer.MAX_VALUE);
                if (needsSolving) {
                    this.mSystem.minimize();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!needsSolving) {
                updateFromSolver(this.mSystem, Integer.MAX_VALUE);
                int i2 = 0;
                while (true) {
                    if (i2 >= count) {
                        break;
                    }
                    ConstraintWidget widget2 = (ConstraintWidget) this.mChildren.get(i2);
                    if (widget2.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget2.getWidth() < widget2.getWrapWidth()) {
                        this.flags[2] = USE_SNAPSHOT;
                        break;
                    } else if (widget2.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget2.getHeight() < widget2.getWrapHeight()) {
                        this.flags[2] = USE_SNAPSHOT;
                        break;
                    } else {
                        i2++;
                    }
                }
            } else {
                updateChildrenFromSolver(this.mSystem, Integer.MAX_VALUE, this.flags);
            }
            needsSolving = false;
            if (countSolve < 8 && this.flags[2]) {
                int maxX = 0;
                int maxY = 0;
                for (int i3 = 0; i3 < count; i3++) {
                    ConstraintWidget widget3 = (ConstraintWidget) this.mChildren.get(i3);
                    maxX = Math.max(maxX, widget3.mX + widget3.getWidth());
                    maxY = Math.max(maxY, widget3.mY + widget3.getHeight());
                }
                int maxX2 = Math.max(this.mMinWidth, maxX);
                int maxY2 = Math.max(this.mMinHeight, maxY);
                if (originalHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT && getWidth() < maxX2) {
                    setWidth(maxX2);
                    this.mHorizontalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
                    wrap_override = USE_SNAPSHOT;
                    needsSolving = USE_SNAPSHOT;
                }
                if (originalVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT && getHeight() < maxY2) {
                    setHeight(maxY2);
                    this.mVerticalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
                    wrap_override = USE_SNAPSHOT;
                    needsSolving = USE_SNAPSHOT;
                }
            }
            int width = Math.max(this.mMinWidth, getWidth());
            if (width > getWidth()) {
                setWidth(width);
                this.mHorizontalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED;
                wrap_override = USE_SNAPSHOT;
                needsSolving = USE_SNAPSHOT;
            }
            int height = Math.max(this.mMinHeight, getHeight());
            if (height > getHeight()) {
                setHeight(height);
                this.mVerticalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED;
                wrap_override = USE_SNAPSHOT;
                needsSolving = USE_SNAPSHOT;
            }
            if (!wrap_override) {
                if (this.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT && prew > 0 && getWidth() > prew) {
                    this.mWidthMeasuredTooSmall = USE_SNAPSHOT;
                    wrap_override = USE_SNAPSHOT;
                    this.mHorizontalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED;
                    setWidth(prew);
                    needsSolving = USE_SNAPSHOT;
                }
                if (this.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT && preh > 0 && getHeight() > preh) {
                    this.mHeightMeasuredTooSmall = USE_SNAPSHOT;
                    wrap_override = USE_SNAPSHOT;
                    this.mVerticalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED;
                    setHeight(preh);
                    needsSolving = USE_SNAPSHOT;
                }
            }
        }
        if (this.mParent == null) {
            this.mX = prex;
            this.mY = prey;
        } else {
            int width2 = Math.max(this.mMinWidth, getWidth());
            int height2 = Math.max(this.mMinHeight, getHeight());
            this.mSnapshot.applyTo(this);
            setWidth(this.mPaddingLeft + width2 + this.mPaddingRight);
            setHeight(this.mPaddingTop + height2 + this.mPaddingBottom);
        }
        if (wrap_override) {
            this.mHorizontalDimensionBehaviour = originalHorizontalDimensionBehaviour;
            this.mVerticalDimensionBehaviour = originalVerticalDimensionBehaviour;
        }
        resetSolverVariables(this.mSystem.getCache());
        if (this == getRootConstraintContainer()) {
            updateDrawPosition();
        }
    }

    static int setGroup(ConstraintAnchor anchor, int group) {
        int oldGroup = anchor.mGroup;
        if (anchor.mOwner.getParent() == null) {
            return group;
        }
        if (oldGroup <= group) {
            return oldGroup;
        }
        anchor.mGroup = group;
        ConstraintAnchor opposite = anchor.getOpposite();
        ConstraintAnchor target = anchor.mTarget;
        if (opposite != null) {
            group = setGroup(opposite, group);
        }
        if (target != null) {
            group = setGroup(target, group);
        }
        if (opposite != null) {
            group = setGroup(opposite, group);
        }
        anchor.mGroup = group;
        return group;
    }

    public int layoutFindGroupsSimple() {
        int size = this.mChildren.size();
        for (int j = 0; j < size; j++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(j);
            widget.mLeft.mGroup = 0;
            widget.mRight.mGroup = 0;
            widget.mTop.mGroup = 1;
            widget.mBottom.mGroup = 1;
            widget.mBaseline.mGroup = 1;
        }
        return 2;
    }

    public void findHorizontalWrapRecursive(ConstraintWidget widget, boolean[] flags2) {
        boolean z = false;
        if (widget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mDimensionRatio > 0.0f) {
            flags2[0] = false;
        } else if (widget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mMatchConstraintDefaultWidth == 2) {
            flags2[0] = false;
        } else {
            int w = widget.getOptimizerWrapWidth();
            if (widget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mVerticalDimensionBehaviour != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mDimensionRatio > 0.0f) {
                flags2[0] = false;
                return;
            }
            int distToRight = w;
            int distToLeft = w;
            ConstraintWidget leftWidget = null;
            ConstraintWidget rightWidget = null;
            widget.mHorizontalWrapVisited = USE_SNAPSHOT;
            if (widget instanceof Guideline) {
                Guideline guideline = (Guideline) widget;
                if (guideline.getOrientation() == 1) {
                    distToLeft = 0;
                    distToRight = 0;
                    if (guideline.getRelativeBegin() != -1) {
                        distToLeft = guideline.getRelativeBegin();
                    } else if (guideline.getRelativeEnd() != -1) {
                        distToRight = guideline.getRelativeEnd();
                    } else if (guideline.getRelativePercent() != -1.0f) {
                        flags2[0] = false;
                        return;
                    }
                }
            } else if (!widget.mRight.isConnected() && !widget.mLeft.isConnected()) {
                distToLeft = w + widget.getX();
            } else if (widget.mRight.mTarget != null && widget.mLeft.mTarget != null && widget.mIsWidthWrapContent && widget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                flags2[0] = false;
                return;
            } else if (widget.mRight.mTarget == null || widget.mLeft.mTarget == null || (widget.mRight.mTarget != widget.mLeft.mTarget && (widget.mRight.mTarget.mOwner != widget.mLeft.mTarget.mOwner || widget.mRight.mTarget.mOwner == widget.mParent))) {
                if (widget.mRight.mTarget != null) {
                    rightWidget = widget.mRight.mTarget.mOwner;
                    distToRight += widget.mRight.getMargin();
                    if (!rightWidget.isRoot() && !rightWidget.mHorizontalWrapVisited) {
                        findHorizontalWrapRecursive(rightWidget, flags2);
                    }
                }
                if (widget.mLeft.mTarget != null) {
                    leftWidget = widget.mLeft.mTarget.mOwner;
                    distToLeft = w + widget.mLeft.getMargin();
                    if (!leftWidget.isRoot() && !leftWidget.mHorizontalWrapVisited) {
                        findHorizontalWrapRecursive(leftWidget, flags2);
                    }
                }
                if (widget.mRight.mTarget != null && !rightWidget.isRoot()) {
                    if (widget.mRight.mTarget.mType == ConstraintAnchor.Type.RIGHT) {
                        distToRight += rightWidget.mDistToRight - rightWidget.getOptimizerWrapWidth();
                    } else if (widget.mRight.mTarget.getType() == ConstraintAnchor.Type.LEFT) {
                        distToRight += rightWidget.mDistToRight;
                    }
                    widget.mRightHasCentered = rightWidget.mRightHasCentered || !(rightWidget.mLeft.mTarget == null || rightWidget.mRight.mTarget == null || rightWidget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT);
                    if (widget.mRightHasCentered && (rightWidget.mLeft.mTarget == null || rightWidget.mLeft.mTarget.mOwner != widget)) {
                        distToRight += distToRight - rightWidget.mDistToRight;
                    }
                }
                if (widget.mLeft.mTarget != null && !leftWidget.isRoot()) {
                    if (widget.mLeft.mTarget.getType() == ConstraintAnchor.Type.LEFT) {
                        distToLeft += leftWidget.mDistToLeft - leftWidget.getOptimizerWrapWidth();
                    } else if (widget.mLeft.mTarget.getType() == ConstraintAnchor.Type.RIGHT) {
                        distToLeft += leftWidget.mDistToLeft;
                    }
                    if (leftWidget.mLeftHasCentered || !(leftWidget.mLeft.mTarget == null || leftWidget.mRight.mTarget == null || leftWidget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)) {
                        z = true;
                    }
                    widget.mLeftHasCentered = z;
                    if (widget.mLeftHasCentered && (leftWidget.mRight.mTarget == null || leftWidget.mRight.mTarget.mOwner != widget)) {
                        distToLeft += distToLeft - leftWidget.mDistToLeft;
                    }
                }
            } else {
                flags2[0] = false;
                return;
            }
            if (widget.getVisibility() == 8) {
                distToLeft -= widget.mWidth;
                distToRight -= widget.mWidth;
            }
            widget.mDistToLeft = distToLeft;
            widget.mDistToRight = distToRight;
        }
    }

    public void findVerticalWrapRecursive(ConstraintWidget widget, boolean[] flags2) {
        boolean z = false;
        if (widget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mHorizontalDimensionBehaviour != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mDimensionRatio > 0.0f) {
            flags2[0] = false;
        } else if (widget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && widget.mMatchConstraintDefaultHeight == 2) {
            flags2[0] = false;
        } else {
            int h = widget.getOptimizerWrapHeight();
            int distToTop = h;
            int distToBottom = h;
            ConstraintWidget topWidget = null;
            ConstraintWidget bottomWidget = null;
            widget.mVerticalWrapVisited = USE_SNAPSHOT;
            if (widget instanceof Guideline) {
                Guideline guideline = (Guideline) widget;
                if (guideline.getOrientation() == 0) {
                    distToTop = 0;
                    distToBottom = 0;
                    if (guideline.getRelativeBegin() != -1) {
                        distToTop = guideline.getRelativeBegin();
                    } else if (guideline.getRelativeEnd() != -1) {
                        distToBottom = guideline.getRelativeEnd();
                    } else if (guideline.getRelativePercent() != -1.0f) {
                        flags2[0] = false;
                        return;
                    }
                }
            } else if (widget.mBaseline.mTarget == null && widget.mTop.mTarget == null && widget.mBottom.mTarget == null) {
                distToTop += widget.getY();
            } else if (widget.mBottom.mTarget != null && widget.mTop.mTarget != null && widget.mIsHeightWrapContent && widget.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                flags2[0] = false;
                return;
            } else if (widget.mBottom.mTarget != null && widget.mTop.mTarget != null && (widget.mBottom.mTarget == widget.mTop.mTarget || (widget.mBottom.mTarget.mOwner == widget.mTop.mTarget.mOwner && widget.mBottom.mTarget.mOwner != widget.mParent))) {
                flags2[0] = false;
                return;
            } else if (!widget.mBaseline.isConnected()) {
                if (widget.mTop.isConnected()) {
                    topWidget = widget.mTop.mTarget.getOwner();
                    distToTop += widget.mTop.getMargin();
                    if (!topWidget.isRoot() && !topWidget.mVerticalWrapVisited) {
                        findVerticalWrapRecursive(topWidget, flags2);
                    }
                }
                if (widget.mBottom.isConnected()) {
                    bottomWidget = widget.mBottom.mTarget.getOwner();
                    distToBottom = h + widget.mBottom.getMargin();
                    if (!bottomWidget.isRoot() && !bottomWidget.mVerticalWrapVisited) {
                        findVerticalWrapRecursive(bottomWidget, flags2);
                    }
                }
                if (widget.mTop.mTarget != null && !topWidget.isRoot()) {
                    if (widget.mTop.mTarget.getType() == ConstraintAnchor.Type.TOP) {
                        distToTop += topWidget.mDistToTop - topWidget.getOptimizerWrapHeight();
                    } else if (widget.mTop.mTarget.getType() == ConstraintAnchor.Type.BOTTOM) {
                        distToTop += topWidget.mDistToTop;
                    }
                    widget.mTopHasCentered = topWidget.mTopHasCentered || !(topWidget.mTop.mTarget == null || topWidget.mTop.mTarget.mOwner == widget || topWidget.mBottom.mTarget == null || topWidget.mBottom.mTarget.mOwner == widget || topWidget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT);
                    if (widget.mTopHasCentered && (topWidget.mBottom.mTarget == null || topWidget.mBottom.mTarget.mOwner != widget)) {
                        distToTop += distToTop - topWidget.mDistToTop;
                    }
                }
                if (widget.mBottom.mTarget != null && !bottomWidget.isRoot()) {
                    if (widget.mBottom.mTarget.getType() == ConstraintAnchor.Type.BOTTOM) {
                        distToBottom += bottomWidget.mDistToBottom - bottomWidget.getOptimizerWrapHeight();
                    } else if (widget.mBottom.mTarget.getType() == ConstraintAnchor.Type.TOP) {
                        distToBottom += bottomWidget.mDistToBottom;
                    }
                    if (bottomWidget.mBottomHasCentered || !(bottomWidget.mTop.mTarget == null || bottomWidget.mTop.mTarget.mOwner == widget || bottomWidget.mBottom.mTarget == null || bottomWidget.mBottom.mTarget.mOwner == widget || bottomWidget.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)) {
                        z = true;
                    }
                    widget.mBottomHasCentered = z;
                    if (widget.mBottomHasCentered && (bottomWidget.mTop.mTarget == null || bottomWidget.mTop.mTarget.mOwner != widget)) {
                        distToBottom += distToBottom - bottomWidget.mDistToBottom;
                    }
                }
            } else {
                ConstraintWidget baseLineWidget = widget.mBaseline.mTarget.getOwner();
                if (!baseLineWidget.mVerticalWrapVisited) {
                    findVerticalWrapRecursive(baseLineWidget, flags2);
                }
                int distToTop2 = Math.max((baseLineWidget.mDistToTop - baseLineWidget.mHeight) + h, h);
                int distToBottom2 = Math.max((baseLineWidget.mDistToBottom - baseLineWidget.mHeight) + h, h);
                if (widget.getVisibility() == 8) {
                    distToTop2 -= widget.mHeight;
                    distToBottom2 -= widget.mHeight;
                }
                widget.mDistToTop = distToTop2;
                widget.mDistToBottom = distToBottom2;
                return;
            }
            if (widget.getVisibility() == 8) {
                distToTop -= widget.mHeight;
                distToBottom -= widget.mHeight;
            }
            widget.mDistToTop = distToTop;
            widget.mDistToBottom = distToBottom;
        }
    }

    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public void findWrapSize(java.util.ArrayList<android.support.constraint.solver.widgets.ConstraintWidget> r19, boolean[] r20) {
        /*
            r18 = this;
            r13 = 0
            r11 = 0
            r12 = 0
            r8 = 0
            r10 = 0
            r9 = 0
            int r14 = r19.size()
            r16 = 1
            r17 = 0
            r20[r17] = r16
            r6 = 0
        L_0x0011:
            if (r6 < r14) goto L_0x0047
            int r7 = java.lang.Math.max(r11, r12)     // Catch:{ all -> 0x00ee }
            r0 = r18
            int r0 = r0.mMinWidth     // Catch:{ all -> 0x00ee }
            r16 = r0
            int r17 = java.lang.Math.max(r7, r10)     // Catch:{ all -> 0x00ee }
            int r16 = java.lang.Math.max(r16, r17)     // Catch:{ all -> 0x00ee }
            r0 = r16
            r1 = r18
            r1.mWrapWidth = r0     // Catch:{ all -> 0x00ee }
            int r7 = java.lang.Math.max(r13, r8)     // Catch:{ all -> 0x00ee }
            r0 = r18
            int r0 = r0.mMinHeight     // Catch:{ all -> 0x00ee }
            r16 = r0
            int r17 = java.lang.Math.max(r7, r9)     // Catch:{ all -> 0x00ee }
            int r16 = java.lang.Math.max(r16, r17)     // Catch:{ all -> 0x00ee }
            r0 = r16
            r1 = r18
            r1.mWrapHeight = r0     // Catch:{ all -> 0x00ee }
            r6 = 0
        L_0x0044:
            if (r6 < r14) goto L_0x01a2
            return
        L_0x0047:
            r0 = r19
            java.lang.Object r15 = r0.get(r6)     // Catch:{ all -> 0x00ee }
            android.support.constraint.solver.widgets.ConstraintWidget r15 = (android.support.constraint.solver.widgets.ConstraintWidget) r15     // Catch:{ all -> 0x00ee }
            boolean r16 = r15.isRoot()     // Catch:{ all -> 0x00ee }
            if (r16 != 0) goto L_0x00e1
            boolean r0 = r15.mHorizontalWrapVisited     // Catch:{ all -> 0x00ee }
            r16 = r0
            if (r16 == 0) goto L_0x00e5
        L_0x005b:
            r16 = 0
            boolean r16 = r20[r16]     // Catch:{ all -> 0x00ee }
            if (r16 == 0) goto L_0x00f3
            boolean r0 = r15.mVerticalWrapVisited     // Catch:{ all -> 0x00ee }
            r16 = r0
            if (r16 == 0) goto L_0x0126
        L_0x0067:
            r16 = 0
            boolean r16 = r20[r16]     // Catch:{ all -> 0x00ee }
            if (r16 == 0) goto L_0x012f
            int r0 = r15.mDistToLeft     // Catch:{ all -> 0x00ee }
            r16 = r0
            int r0 = r15.mDistToRight     // Catch:{ all -> 0x00ee }
            r17 = r0
            int r16 = r16 + r17
            int r17 = r15.getWidth()     // Catch:{ all -> 0x00ee }
            int r5 = r16 - r17
            int r0 = r15.mDistToTop     // Catch:{ all -> 0x00ee }
            r16 = r0
            int r0 = r15.mDistToBottom     // Catch:{ all -> 0x00ee }
            r17 = r0
            int r16 = r16 + r17
            int r17 = r15.getHeight()     // Catch:{ all -> 0x00ee }
            int r4 = r16 - r17
            android.support.constraint.solver.widgets.ConstraintWidget$DimensionBehaviour r0 = r15.mHorizontalDimensionBehaviour     // Catch:{ all -> 0x00ee }
            r16 = r0
            android.support.constraint.solver.widgets.ConstraintWidget$DimensionBehaviour r17 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_PARENT     // Catch:{ all -> 0x00ee }
            r0 = r16
            r1 = r17
            if (r0 == r1) goto L_0x0162
        L_0x0099:
            android.support.constraint.solver.widgets.ConstraintWidget$DimensionBehaviour r0 = r15.mVerticalDimensionBehaviour     // Catch:{ all -> 0x00ee }
            r16 = r0
            android.support.constraint.solver.widgets.ConstraintWidget$DimensionBehaviour r17 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_PARENT     // Catch:{ all -> 0x00ee }
            r0 = r16
            r1 = r17
            if (r0 == r1) goto L_0x0180
        L_0x00a5:
            int r16 = r15.getVisibility()     // Catch:{ all -> 0x00ee }
            r17 = 8
            r0 = r16
            r1 = r17
            if (r0 == r1) goto L_0x019e
        L_0x00b1:
            int r0 = r15.mDistToLeft     // Catch:{ all -> 0x00ee }
            r16 = r0
            r0 = r16
            int r11 = java.lang.Math.max(r11, r0)     // Catch:{ all -> 0x00ee }
            int r0 = r15.mDistToRight     // Catch:{ all -> 0x00ee }
            r16 = r0
            r0 = r16
            int r12 = java.lang.Math.max(r12, r0)     // Catch:{ all -> 0x00ee }
            int r0 = r15.mDistToBottom     // Catch:{ all -> 0x00ee }
            r16 = r0
            r0 = r16
            int r8 = java.lang.Math.max(r8, r0)     // Catch:{ all -> 0x00ee }
            int r0 = r15.mDistToTop     // Catch:{ all -> 0x00ee }
            r16 = r0
            r0 = r16
            int r13 = java.lang.Math.max(r13, r0)     // Catch:{ all -> 0x00ee }
            int r10 = java.lang.Math.max(r10, r5)     // Catch:{ all -> 0x00ee }
            int r9 = java.lang.Math.max(r9, r4)     // Catch:{ all -> 0x00ee }
        L_0x00e1:
            int r6 = r6 + 1
            goto L_0x0011
        L_0x00e5:
            r0 = r18
            r1 = r20
            r0.findHorizontalWrapRecursive(r15, r1)     // Catch:{ all -> 0x00ee }
            goto L_0x005b
        L_0x00ee:
            r2 = move-exception
            r6 = 0
        L_0x00f0:
            if (r6 < r14) goto L_0x01d2
            throw r2
        L_0x00f3:
            r6 = 0
        L_0x00f4:
            if (r6 < r14) goto L_0x00f7
            return
        L_0x00f7:
            r0 = r19
            java.lang.Object r3 = r0.get(r6)
            android.support.constraint.solver.widgets.ConstraintWidget r3 = (android.support.constraint.solver.widgets.ConstraintWidget) r3
            r16 = 0
            r0 = r16
            r3.mHorizontalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mVerticalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mLeftHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mRightHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mTopHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mBottomHasCentered = r0
            int r6 = r6 + 1
            goto L_0x00f4
        L_0x0126:
            r0 = r18
            r1 = r20
            r0.findVerticalWrapRecursive(r15, r1)     // Catch:{ all -> 0x00ee }
            goto L_0x0067
        L_0x012f:
            r6 = 0
        L_0x0130:
            if (r6 < r14) goto L_0x0133
            return
        L_0x0133:
            r0 = r19
            java.lang.Object r3 = r0.get(r6)
            android.support.constraint.solver.widgets.ConstraintWidget r3 = (android.support.constraint.solver.widgets.ConstraintWidget) r3
            r16 = 0
            r0 = r16
            r3.mHorizontalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mVerticalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mLeftHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mRightHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mTopHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mBottomHasCentered = r0
            int r6 = r6 + 1
            goto L_0x0130
        L_0x0162:
            int r16 = r15.getWidth()     // Catch:{ all -> 0x00ee }
            android.support.constraint.solver.widgets.ConstraintAnchor r0 = r15.mLeft     // Catch:{ all -> 0x00ee }
            r17 = r0
            r0 = r17
            int r0 = r0.mMargin     // Catch:{ all -> 0x00ee }
            r17 = r0
            int r16 = r16 + r17
            android.support.constraint.solver.widgets.ConstraintAnchor r0 = r15.mRight     // Catch:{ all -> 0x00ee }
            r17 = r0
            r0 = r17
            int r0 = r0.mMargin     // Catch:{ all -> 0x00ee }
            r17 = r0
            int r5 = r16 + r17
            goto L_0x0099
        L_0x0180:
            int r16 = r15.getHeight()     // Catch:{ all -> 0x00ee }
            android.support.constraint.solver.widgets.ConstraintAnchor r0 = r15.mTop     // Catch:{ all -> 0x00ee }
            r17 = r0
            r0 = r17
            int r0 = r0.mMargin     // Catch:{ all -> 0x00ee }
            r17 = r0
            int r16 = r16 + r17
            android.support.constraint.solver.widgets.ConstraintAnchor r0 = r15.mBottom     // Catch:{ all -> 0x00ee }
            r17 = r0
            r0 = r17
            int r0 = r0.mMargin     // Catch:{ all -> 0x00ee }
            r17 = r0
            int r4 = r16 + r17
            goto L_0x00a5
        L_0x019e:
            r5 = 0
            r4 = 0
            goto L_0x00b1
        L_0x01a2:
            r0 = r19
            java.lang.Object r3 = r0.get(r6)
            android.support.constraint.solver.widgets.ConstraintWidget r3 = (android.support.constraint.solver.widgets.ConstraintWidget) r3
            r16 = 0
            r0 = r16
            r3.mHorizontalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mVerticalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mLeftHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mRightHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mTopHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mBottomHasCentered = r0
            int r6 = r6 + 1
            goto L_0x0044
        L_0x01d2:
            r0 = r19
            java.lang.Object r3 = r0.get(r6)
            android.support.constraint.solver.widgets.ConstraintWidget r3 = (android.support.constraint.solver.widgets.ConstraintWidget) r3
            r16 = 0
            r0 = r16
            r3.mHorizontalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mVerticalWrapVisited = r0
            r16 = 0
            r0 = r16
            r3.mLeftHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mRightHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mTopHasCentered = r0
            r16 = 0
            r0 = r16
            r3.mBottomHasCentered = r0
            int r6 = r6 + 1
            goto L_0x00f0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.constraint.solver.widgets.ConstraintWidgetContainer.findWrapSize(java.util.ArrayList, boolean[]):void");
    }

    public int layoutFindGroups() {
        int index;
        ConstraintAnchor.Type[] dir = {ConstraintAnchor.Type.LEFT, ConstraintAnchor.Type.RIGHT, ConstraintAnchor.Type.TOP, ConstraintAnchor.Type.BASELINE, ConstraintAnchor.Type.BOTTOM};
        int label = 1;
        int size = this.mChildren.size();
        for (int j = 0; j < size; j++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(j);
            ConstraintAnchor anchor = widget.mLeft;
            if (anchor.mTarget == null) {
                anchor.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(anchor, label) == label) {
                label++;
            }
            ConstraintAnchor anchor2 = widget.mTop;
            if (anchor2.mTarget == null) {
                anchor2.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(anchor2, label) == label) {
                label++;
            }
            ConstraintAnchor anchor3 = widget.mRight;
            if (anchor3.mTarget == null) {
                anchor3.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(anchor3, label) == label) {
                label++;
            }
            ConstraintAnchor anchor4 = widget.mBottom;
            if (anchor4.mTarget == null) {
                anchor4.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(anchor4, label) == label) {
                label++;
            }
            ConstraintAnchor anchor5 = widget.mBaseline;
            if (anchor5.mTarget == null) {
                anchor5.mGroup = Integer.MAX_VALUE;
            } else if (setGroup(anchor5, label) == label) {
                label++;
            }
        }
        boolean notDone = USE_SNAPSHOT;
        int count = 0;
        int fix = 0;
        while (notDone) {
            notDone = false;
            count++;
            for (int j2 = 0; j2 < size; j2++) {
                ConstraintWidget widget2 = (ConstraintWidget) this.mChildren.get(j2);
                for (ConstraintAnchor.Type type : dir) {
                    ConstraintAnchor anchor6 = null;
                    switch (type) {
                        case LEFT:
                            anchor6 = widget2.mLeft;
                            break;
                        case TOP:
                            anchor6 = widget2.mTop;
                            break;
                        case RIGHT:
                            anchor6 = widget2.mRight;
                            break;
                        case BOTTOM:
                            anchor6 = widget2.mBottom;
                            break;
                        case BASELINE:
                            anchor6 = widget2.mBaseline;
                            break;
                    }
                    ConstraintAnchor target = anchor6.mTarget;
                    if (target != null) {
                        if (!(target.mOwner.getParent() == null || target.mGroup == anchor6.mGroup)) {
                            int i = anchor6.mGroup <= target.mGroup ? anchor6.mGroup : target.mGroup;
                            anchor6.mGroup = i;
                            target.mGroup = i;
                            fix++;
                            notDone = USE_SNAPSHOT;
                        }
                        ConstraintAnchor opposite = target.getOpposite();
                        if (!(opposite == null || opposite.mGroup == anchor6.mGroup)) {
                            int i2 = anchor6.mGroup <= opposite.mGroup ? anchor6.mGroup : opposite.mGroup;
                            anchor6.mGroup = i2;
                            opposite.mGroup = i2;
                            fix++;
                            notDone = USE_SNAPSHOT;
                        }
                    }
                }
            }
        }
        int[] table = new int[((this.mChildren.size() * dir.length) + 1)];
        Arrays.fill(table, -1);
        int j3 = 0;
        int index2 = 0;
        while (j3 < size) {
            ConstraintWidget widget3 = (ConstraintWidget) this.mChildren.get(j3);
            ConstraintAnchor anchor7 = widget3.mLeft;
            if (anchor7.mGroup == Integer.MAX_VALUE) {
                index = index2;
            } else {
                int g = anchor7.mGroup;
                if (table[g] != -1) {
                    index = index2;
                } else {
                    index = index2 + 1;
                    table[g] = index2;
                }
                anchor7.mGroup = table[g];
            }
            ConstraintAnchor anchor8 = widget3.mTop;
            if (anchor8.mGroup != Integer.MAX_VALUE) {
                int g2 = anchor8.mGroup;
                if (table[g2] == -1) {
                    table[g2] = index;
                    index++;
                }
                anchor8.mGroup = table[g2];
            }
            ConstraintAnchor anchor9 = widget3.mRight;
            if (anchor9.mGroup != Integer.MAX_VALUE) {
                int g3 = anchor9.mGroup;
                if (table[g3] == -1) {
                    table[g3] = index;
                    index++;
                }
                anchor9.mGroup = table[g3];
            }
            ConstraintAnchor anchor10 = widget3.mBottom;
            if (anchor10.mGroup != Integer.MAX_VALUE) {
                int g4 = anchor10.mGroup;
                if (table[g4] == -1) {
                    table[g4] = index;
                    index++;
                }
                anchor10.mGroup = table[g4];
            }
            ConstraintAnchor anchor11 = widget3.mBaseline;
            if (anchor11.mGroup != Integer.MAX_VALUE) {
                int g5 = anchor11.mGroup;
                if (table[g5] == -1) {
                    table[g5] = index;
                    index++;
                }
                anchor11.mGroup = table[g5];
            }
            j3++;
            index2 = index;
        }
        return index2;
    }

    public void layoutWithGroup(int numOfGroups) {
        int prex = this.mX;
        int prey = this.mY;
        if (this.mParent == null) {
            this.mX = 0;
            this.mY = 0;
        } else {
            if (this.mSnapshot == null) {
                this.mSnapshot = new Snapshot(this);
            }
            this.mSnapshot.updateFrom(this);
            this.mX = 0;
            this.mY = 0;
            resetAnchors();
            resetSolverVariables(this.mSystem.getCache());
        }
        int count = this.mChildren.size();
        for (int i = 0; i < count; i++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(i);
            if (widget instanceof WidgetContainer) {
                ((WidgetContainer) widget).layout();
            }
        }
        this.mLeft.mGroup = 0;
        this.mRight.mGroup = 0;
        this.mTop.mGroup = 1;
        this.mBottom.mGroup = 1;
        this.mSystem.reset();
        for (int i2 = 0; i2 < numOfGroups; i2++) {
            try {
                addToSolver(this.mSystem, i2);
                this.mSystem.minimize();
                updateFromSolver(this.mSystem, i2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateFromSolver(this.mSystem, -2);
        }
        if (this.mParent == null) {
            this.mX = prex;
            this.mY = prey;
        } else {
            int width = getWidth();
            int height = getHeight();
            this.mSnapshot.applyTo(this);
            setWidth(width);
            setHeight(height);
        }
        if (this == getRootConstraintContainer()) {
            updateDrawPosition();
        }
    }

    public boolean handlesInternalConstraints() {
        return false;
    }

    public ArrayList<Guideline> getVerticalGuidelines() {
        ArrayList<Guideline> guidelines = new ArrayList<>();
        int mChildrenSize = this.mChildren.size();
        for (int i = 0; i < mChildrenSize; i++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(i);
            if (widget instanceof Guideline) {
                Guideline guideline = (Guideline) widget;
                if (guideline.getOrientation() == 1) {
                    guidelines.add(guideline);
                }
            }
        }
        return guidelines;
    }

    public ArrayList<Guideline> getHorizontalGuidelines() {
        ArrayList<Guideline> guidelines = new ArrayList<>();
        int mChildrenSize = this.mChildren.size();
        for (int i = 0; i < mChildrenSize; i++) {
            ConstraintWidget widget = (ConstraintWidget) this.mChildren.get(i);
            if (widget instanceof Guideline) {
                Guideline guideline = (Guideline) widget;
                if (guideline.getOrientation() == 0) {
                    guidelines.add(guideline);
                }
            }
        }
        return guidelines;
    }

    public LinearSystem getSystem() {
        return this.mSystem;
    }

    private void resetChains() {
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
    }

    /* access modifiers changed from: package-private */
    public void addChain(ConstraintWidget constraintWidget, int type) {
        ConstraintWidget widget = constraintWidget;
        if (type == 0) {
            while (widget.mLeft.mTarget != null && widget.mLeft.mTarget.mOwner.mRight.mTarget != null && widget.mLeft.mTarget.mOwner.mRight.mTarget == widget.mLeft && widget.mLeft.mTarget.mOwner != widget) {
                widget = widget.mLeft.mTarget.mOwner;
            }
            addHorizontalChain(widget);
        } else if (type == 1) {
            while (widget.mTop.mTarget != null && widget.mTop.mTarget.mOwner.mBottom.mTarget != null && widget.mTop.mTarget.mOwner.mBottom.mTarget == widget.mTop && widget.mTop.mTarget.mOwner != widget) {
                widget = widget.mTop.mTarget.mOwner;
            }
            addVerticalChain(widget);
        }
    }

    private void addHorizontalChain(ConstraintWidget widget) {
        int i = 0;
        while (i < this.mHorizontalChainsSize) {
            if (this.mHorizontalChainsArray[i] != widget) {
                i++;
            } else {
                return;
            }
        }
        if (this.mHorizontalChainsSize + 1 >= this.mHorizontalChainsArray.length) {
            this.mHorizontalChainsArray = (ConstraintWidget[]) Arrays.copyOf(this.mHorizontalChainsArray, this.mHorizontalChainsArray.length * 2);
        }
        this.mHorizontalChainsArray[this.mHorizontalChainsSize] = widget;
        this.mHorizontalChainsSize++;
    }

    private void addVerticalChain(ConstraintWidget widget) {
        int i = 0;
        while (i < this.mVerticalChainsSize) {
            if (this.mVerticalChainsArray[i] != widget) {
                i++;
            } else {
                return;
            }
        }
        if (this.mVerticalChainsSize + 1 >= this.mVerticalChainsArray.length) {
            this.mVerticalChainsArray = (ConstraintWidget[]) Arrays.copyOf(this.mVerticalChainsArray, this.mVerticalChainsArray.length * 2);
        }
        this.mVerticalChainsArray[this.mVerticalChainsSize] = widget;
        this.mVerticalChainsSize++;
    }

    private int countMatchConstraintsChainedWidgets(LinearSystem system, ConstraintWidget[] chainEnds, ConstraintWidget widget, int direction, boolean[] flags2) {
        int count = 0;
        flags2[0] = USE_SNAPSHOT;
        flags2[1] = false;
        chainEnds[0] = null;
        chainEnds[2] = null;
        chainEnds[1] = null;
        chainEnds[3] = null;
        if (direction != 0) {
            boolean fixedPosition = USE_SNAPSHOT;
            ConstraintWidget constraintWidget = widget;
            ConstraintWidget last = null;
            if (!(widget.mTop.mTarget == null || widget.mTop.mTarget.mOwner == this)) {
                fixedPosition = false;
            }
            widget.mVerticalNextWidget = null;
            ConstraintWidget firstVisible = null;
            if (widget.getVisibility() != 8) {
                firstVisible = widget;
            }
            ConstraintWidget lastVisible = firstVisible;
            ConstraintWidget widget2 = widget;
            while (widget2.mBottom.mTarget != null) {
                widget2.mVerticalNextWidget = null;
                if (widget2.getVisibility() == 8) {
                    system.addEquality(widget2.mTop.mSolverVariable, widget2.mTop.mTarget.mSolverVariable, 0, 5);
                    system.addEquality(widget2.mBottom.mSolverVariable, widget2.mTop.mSolverVariable, 0, 5);
                } else {
                    if (firstVisible == null) {
                        firstVisible = widget2;
                    }
                    if (!(lastVisible == null || lastVisible == widget2)) {
                        lastVisible.mVerticalNextWidget = widget2;
                    }
                    lastVisible = widget2;
                }
                if (widget2.getVisibility() != 8 && widget2.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    if (widget2.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                        flags2[0] = false;
                    }
                    if (widget2.mDimensionRatio <= 0.0f) {
                        flags2[0] = false;
                        if (count + 1 >= this.mMatchConstraintsChainedWidgets.length) {
                            this.mMatchConstraintsChainedWidgets = (ConstraintWidget[]) Arrays.copyOf(this.mMatchConstraintsChainedWidgets, this.mMatchConstraintsChainedWidgets.length * 2);
                        }
                        this.mMatchConstraintsChainedWidgets[count] = widget2;
                        count++;
                    }
                }
                if (widget2.mBottom.mTarget.mOwner.mTop.mTarget == null || widget2.mBottom.mTarget.mOwner.mTop.mTarget.mOwner != widget2 || widget2.mBottom.mTarget.mOwner == widget2) {
                    break;
                }
                widget2 = widget2.mBottom.mTarget.mOwner;
                last = widget2;
            }
            if (!(widget2.mBottom.mTarget == null || widget2.mBottom.mTarget.mOwner == this)) {
                fixedPosition = false;
            }
            if (widget.mTop.mTarget == null || last.mBottom.mTarget == null) {
                flags2[1] = USE_SNAPSHOT;
            }
            widget.mVerticalChainFixedPosition = fixedPosition;
            last.mVerticalNextWidget = null;
            chainEnds[0] = widget;
            chainEnds[2] = firstVisible;
            chainEnds[1] = last;
            chainEnds[3] = lastVisible;
            ConstraintWidget constraintWidget2 = widget2;
        } else {
            boolean fixedPosition2 = USE_SNAPSHOT;
            ConstraintWidget constraintWidget3 = widget;
            ConstraintWidget last2 = null;
            if (!(widget.mLeft.mTarget == null || widget.mLeft.mTarget.mOwner == this)) {
                fixedPosition2 = false;
            }
            widget.mHorizontalNextWidget = null;
            ConstraintWidget firstVisible2 = null;
            if (widget.getVisibility() != 8) {
                firstVisible2 = widget;
            }
            ConstraintWidget lastVisible2 = firstVisible2;
            ConstraintWidget widget3 = widget;
            while (widget3.mRight.mTarget != null) {
                widget3.mHorizontalNextWidget = null;
                if (widget3.getVisibility() == 8) {
                    system.addEquality(widget3.mLeft.mSolverVariable, widget3.mLeft.mTarget.mSolverVariable, 0, 5);
                    system.addEquality(widget3.mRight.mSolverVariable, widget3.mLeft.mSolverVariable, 0, 5);
                } else {
                    if (firstVisible2 == null) {
                        firstVisible2 = widget3;
                    }
                    if (!(lastVisible2 == null || lastVisible2 == widget3)) {
                        lastVisible2.mHorizontalNextWidget = widget3;
                    }
                    lastVisible2 = widget3;
                }
                if (widget3.getVisibility() != 8 && widget3.mHorizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    if (widget3.mVerticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                        flags2[0] = false;
                    }
                    if (widget3.mDimensionRatio <= 0.0f) {
                        flags2[0] = false;
                        if (count + 1 >= this.mMatchConstraintsChainedWidgets.length) {
                            this.mMatchConstraintsChainedWidgets = (ConstraintWidget[]) Arrays.copyOf(this.mMatchConstraintsChainedWidgets, this.mMatchConstraintsChainedWidgets.length * 2);
                        }
                        this.mMatchConstraintsChainedWidgets[count] = widget3;
                        count++;
                    }
                }
                if (widget3.mRight.mTarget.mOwner.mLeft.mTarget == null || widget3.mRight.mTarget.mOwner.mLeft.mTarget.mOwner != widget3 || widget3.mRight.mTarget.mOwner == widget3) {
                    break;
                }
                widget3 = widget3.mRight.mTarget.mOwner;
                last2 = widget3;
            }
            if (!(widget3.mRight.mTarget == null || widget3.mRight.mTarget.mOwner == this)) {
                fixedPosition2 = false;
            }
            if (widget.mLeft.mTarget == null || last2.mRight.mTarget == null) {
                flags2[1] = USE_SNAPSHOT;
            }
            widget.mHorizontalChainFixedPosition = fixedPosition2;
            last2.mHorizontalNextWidget = null;
            chainEnds[0] = widget;
            chainEnds[2] = firstVisible2;
            chainEnds[1] = last2;
            chainEnds[3] = lastVisible2;
            ConstraintWidget constraintWidget4 = widget3;
        }
        return count;
    }
}
