package android.support.transition;

import android.os.IBinder;
import android.support.annotation.RequiresApi;

@RequiresApi(14)
class WindowIdApi14 implements WindowIdImpl {
    private final IBinder mToken;

    WindowIdApi14(IBinder token) {
        this.mToken = token;
    }

    public boolean equals(Object o) {
        if (o instanceof WindowIdApi14) {
            return ((WindowIdApi14) o).mToken.equals(this.mToken);
        }
        return false;
    }

    public int hashCode() {
        return this.mToken.hashCode();
    }
}
