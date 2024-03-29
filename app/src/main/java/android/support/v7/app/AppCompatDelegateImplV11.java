package android.support.v7.app;

import android.content.Context;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

@RequiresApi(14)
class AppCompatDelegateImplV11 extends AppCompatDelegateImplV9 {
    AppCompatDelegateImplV11(Context context, Window window, AppCompatCallback callback) {
        super(context, window, callback);
    }

    public boolean hasWindowFeature(int featureId) {
        if (!super.hasWindowFeature(featureId)) {
            return this.mWindow.hasFeature(featureId);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public View callActivityOnCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return null;
    }
}
