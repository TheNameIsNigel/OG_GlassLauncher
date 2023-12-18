package android.support.v4.util;

import android.os.Build;
import java.util.Objects;

public class ObjectsCompat {
    private ObjectsCompat() {
    }

    public static boolean equals(Object a, Object b) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.equals(a, b);
        }
        if (a == b) {
            return true;
        }
        if (a != null) {
            return a.equals(b);
        }
        return false;
    }
}
