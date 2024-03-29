package android.support.constraint.solver.widgets;

public class ConstraintHorizontalLayout extends ConstraintWidgetContainer {
    private ContentAlignment mAlignment = ContentAlignment.MIDDLE;

    public enum ContentAlignment {
        BEGIN,
        MIDDLE,
        END,
        TOP,
        VERTICAL_MIDDLE,
        BOTTOM,
        LEFT,
        RIGHT
    }

    public ConstraintHorizontalLayout() {
    }

    public ConstraintHorizontalLayout(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public ConstraintHorizontalLayout(int width, int height) {
        super(width, height);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: android.support.constraint.solver.widgets.ConstraintHorizontalLayout} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: android.support.constraint.solver.widgets.ConstraintHorizontalLayout} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.support.constraint.solver.widgets.ConstraintWidget} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: android.support.constraint.solver.widgets.ConstraintHorizontalLayout} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addToSolver(android.support.constraint.solver.LinearSystem r15, int r16) {
        /*
            r14 = this;
            java.util.ArrayList r1 = r14.mChildren
            int r1 = r1.size()
            if (r1 != 0) goto L_0x000c
        L_0x0008:
            super.addToSolver(r15, r16)
            return
        L_0x000c:
            r2 = r14
            r12 = 0
            java.util.ArrayList r1 = r14.mChildren
            int r13 = r1.size()
        L_0x0014:
            if (r12 < r13) goto L_0x002c
            if (r2 == r14) goto L_0x0008
            android.support.constraint.solver.widgets.ConstraintAnchor$Strength r5 = android.support.constraint.solver.widgets.ConstraintAnchor.Strength.STRONG
            android.support.constraint.solver.widgets.ConstraintHorizontalLayout$ContentAlignment r1 = r14.mAlignment
            android.support.constraint.solver.widgets.ConstraintHorizontalLayout$ContentAlignment r3 = android.support.constraint.solver.widgets.ConstraintHorizontalLayout.ContentAlignment.BEGIN
            if (r1 == r3) goto L_0x006a
        L_0x0020:
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r7 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.RIGHT
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r9 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.RIGHT
            r10 = 0
            r6 = r2
            r8 = r14
            r11 = r5
            r6.connect((android.support.constraint.solver.widgets.ConstraintAnchor.Type) r7, (android.support.constraint.solver.widgets.ConstraintWidget) r8, (android.support.constraint.solver.widgets.ConstraintAnchor.Type) r9, (int) r10, (android.support.constraint.solver.widgets.ConstraintAnchor.Strength) r11)
            goto L_0x0008
        L_0x002c:
            java.util.ArrayList r1 = r14.mChildren
            java.lang.Object r0 = r1.get(r12)
            android.support.constraint.solver.widgets.ConstraintWidget r0 = (android.support.constraint.solver.widgets.ConstraintWidget) r0
            if (r2 != r14) goto L_0x0058
            android.support.constraint.solver.widgets.ConstraintAnchor$Strength r5 = android.support.constraint.solver.widgets.ConstraintAnchor.Strength.STRONG
            android.support.constraint.solver.widgets.ConstraintHorizontalLayout$ContentAlignment r1 = r14.mAlignment
            android.support.constraint.solver.widgets.ConstraintHorizontalLayout$ContentAlignment r3 = android.support.constraint.solver.widgets.ConstraintHorizontalLayout.ContentAlignment.END
            if (r1 == r3) goto L_0x0067
        L_0x003e:
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r1 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.LEFT
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r3 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.LEFT
            r4 = 0
            r0.connect((android.support.constraint.solver.widgets.ConstraintAnchor.Type) r1, (android.support.constraint.solver.widgets.ConstraintWidget) r2, (android.support.constraint.solver.widgets.ConstraintAnchor.Type) r3, (int) r4, (android.support.constraint.solver.widgets.ConstraintAnchor.Strength) r5)
        L_0x0046:
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r1 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.TOP
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r3 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.TOP
            r0.connect((android.support.constraint.solver.widgets.ConstraintAnchor.Type) r1, (android.support.constraint.solver.widgets.ConstraintWidget) r14, (android.support.constraint.solver.widgets.ConstraintAnchor.Type) r3)
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r1 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.BOTTOM
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r3 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.BOTTOM
            r0.connect((android.support.constraint.solver.widgets.ConstraintAnchor.Type) r1, (android.support.constraint.solver.widgets.ConstraintWidget) r14, (android.support.constraint.solver.widgets.ConstraintAnchor.Type) r3)
            r2 = r0
            int r12 = r12 + 1
            goto L_0x0014
        L_0x0058:
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r1 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.LEFT
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r3 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.RIGHT
            r0.connect((android.support.constraint.solver.widgets.ConstraintAnchor.Type) r1, (android.support.constraint.solver.widgets.ConstraintWidget) r2, (android.support.constraint.solver.widgets.ConstraintAnchor.Type) r3)
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r1 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.RIGHT
            android.support.constraint.solver.widgets.ConstraintAnchor$Type r3 = android.support.constraint.solver.widgets.ConstraintAnchor.Type.LEFT
            r2.connect((android.support.constraint.solver.widgets.ConstraintAnchor.Type) r1, (android.support.constraint.solver.widgets.ConstraintWidget) r0, (android.support.constraint.solver.widgets.ConstraintAnchor.Type) r3)
            goto L_0x0046
        L_0x0067:
            android.support.constraint.solver.widgets.ConstraintAnchor$Strength r5 = android.support.constraint.solver.widgets.ConstraintAnchor.Strength.WEAK
            goto L_0x003e
        L_0x006a:
            android.support.constraint.solver.widgets.ConstraintAnchor$Strength r5 = android.support.constraint.solver.widgets.ConstraintAnchor.Strength.WEAK
            goto L_0x0020
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.constraint.solver.widgets.ConstraintHorizontalLayout.addToSolver(android.support.constraint.solver.LinearSystem, int):void");
    }
}
