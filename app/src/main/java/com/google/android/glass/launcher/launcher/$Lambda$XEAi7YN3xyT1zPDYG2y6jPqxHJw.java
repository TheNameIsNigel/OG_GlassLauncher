package com.google.android.glass.launcher.launcher;

/* renamed from: com.google.android.glass.launcher.launcher.-$Lambda$XEAi7YN3xyT1zPDYG2y6jPqxHJw  reason: invalid class name */
final /* synthetic */ class $Lambda$XEAi7YN3xyT1zPDYG2y6jPqxHJw implements Runnable {
    private final /* synthetic */ byte $id;

    /* renamed from: -$f0  reason: not valid java name */
    private final /* synthetic */ Object f8$f0;

    private final /* synthetic */ void $m$0() {
        ((LauncherFragmentAnimator) this.f8$f0).m199lambda$com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_4830();
    }

    private final /* synthetic */ void $m$1() {
        ((LauncherFragmentAnimator) this.f8$f0).m197lambda$com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_2830();
    }

    private final /* synthetic */ void $m$2() {
        ((LauncherFragmentAnimator) this.f8$f0).m198lambda$com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_3977();
    }

    public /* synthetic */ $Lambda$XEAi7YN3xyT1zPDYG2y6jPqxHJw(byte b, Object obj) {
        this.$id = b;
        this.f8$f0 = obj;
    }

    public final void run() {
        switch (this.$id) {
            case 0:
                $m$0();
                return;
            case 1:
                $m$1();
                return;
            case 2:
                $m$2();
                return;
            default:
                throw new AssertionError();
        }
    }
}
