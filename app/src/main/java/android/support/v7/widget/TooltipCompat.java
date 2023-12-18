package android.support.v7.widget;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public class TooltipCompat {
    private static final ViewCompatImpl IMPL;

    private interface ViewCompatImpl {
        void setTooltipText(@NonNull View view, @Nullable CharSequence charSequence);
    }

    private static class BaseViewCompatImpl implements ViewCompatImpl {
        /* synthetic */ BaseViewCompatImpl(BaseViewCompatImpl baseViewCompatImpl) {
            this();
        }

        private BaseViewCompatImpl() {
        }

        public void setTooltipText(@NonNull View view, @Nullable CharSequence tooltipText) {
            TooltipCompatHandler.setTooltipText(view, tooltipText);
        }
    }

    @TargetApi(26)
    private static class Api26ViewCompatImpl implements ViewCompatImpl {
        /* synthetic */ Api26ViewCompatImpl(Api26ViewCompatImpl api26ViewCompatImpl) {
            this();
        }

        private Api26ViewCompatImpl() {
        }

        public void setTooltipText(@NonNull View view, @Nullable CharSequence tooltipText) {
            view.setTooltipText(tooltipText);
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 26) {
            IMPL = new Api26ViewCompatImpl((Api26ViewCompatImpl) null);
        } else {
            IMPL = new BaseViewCompatImpl((BaseViewCompatImpl) null);
        }
    }

    public static void setTooltipText(@NonNull View view, @Nullable CharSequence tooltipText) {
        IMPL.setTooltipText(view, tooltipText);
    }

    private TooltipCompat() {
    }
}
