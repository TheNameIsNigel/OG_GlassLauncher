package android.support.transition;

import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.WindowId;

@RequiresApi(18)
class WindowIdApi18 implements WindowIdImpl {
    private final WindowId mWindowId;

    WindowIdApi18(@NonNull View view) {
        this.mWindowId = view.getWindowId();
    }

    public boolean equals(Object o) {
        if (o instanceof WindowIdApi18) {
            return ((WindowIdApi18) o).mWindowId.equals(this.mWindowId);
        }
        return false;
    }

    public int hashCode() {
        return this.mWindowId.hashCode();
    }
}
