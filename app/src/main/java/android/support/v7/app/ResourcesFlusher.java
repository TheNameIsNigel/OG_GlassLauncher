package android.support.v7.app;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.lang.reflect.Field;

class ResourcesFlusher {
    private static final String TAG = "ResourcesFlusher";
    private static Field sDrawableCacheField;
    private static boolean sDrawableCacheFieldFetched;
    private static Field sResourcesImplField;
    private static boolean sResourcesImplFieldFetched;
    private static Class sThemedResourceCacheClazz;
    private static boolean sThemedResourceCacheClazzFetched;
    private static Field sThemedResourceCache_mUnthemedEntriesField;
    private static boolean sThemedResourceCache_mUnthemedEntriesFieldFetched;

    ResourcesFlusher() {
    }

    static boolean flush(@NonNull Resources resources) {
        if (Build.VERSION.SDK_INT >= 24) {
            return flushNougats(resources);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return flushMarshmallows(resources);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            return flushLollipops(resources);
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.util.Map} */
    /* JADX WARNING: Multi-variable type inference failed */
    @android.support.annotation.RequiresApi(21)
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean flushLollipops(@android.support.annotation.NonNull android.content.res.Resources r7) {
        /*
            r6 = 1
            boolean r4 = sDrawableCacheFieldFetched
            if (r4 != 0) goto L_0x0018
            java.lang.Class<android.content.res.Resources> r4 = android.content.res.Resources.class
            java.lang.String r5 = "mDrawableCache"
            java.lang.reflect.Field r4 = r4.getDeclaredField(r5)     // Catch:{ NoSuchFieldException -> 0x002d }
            sDrawableCacheField = r4     // Catch:{ NoSuchFieldException -> 0x002d }
            java.lang.reflect.Field r4 = sDrawableCacheField     // Catch:{ NoSuchFieldException -> 0x002d }
            r5 = 1
            r4.setAccessible(r5)     // Catch:{ NoSuchFieldException -> 0x002d }
        L_0x0016:
            sDrawableCacheFieldFetched = r6
        L_0x0018:
            java.lang.reflect.Field r4 = sDrawableCacheField
            if (r4 == 0) goto L_0x0043
            r1 = 0
            java.lang.reflect.Field r4 = sDrawableCacheField     // Catch:{ IllegalAccessException -> 0x0038 }
            java.lang.Object r4 = r4.get(r7)     // Catch:{ IllegalAccessException -> 0x0038 }
            r0 = r4
            java.util.Map r0 = (java.util.Map) r0     // Catch:{ IllegalAccessException -> 0x0038 }
            r1 = r0
        L_0x0027:
            if (r1 == 0) goto L_0x0043
            r1.clear()
            return r6
        L_0x002d:
            r3 = move-exception
            java.lang.String r4 = "ResourcesFlusher"
            java.lang.String r5 = "Could not retrieve Resources#mDrawableCache field"
            android.util.Log.e(r4, r5, r3)
            goto L_0x0016
        L_0x0038:
            r2 = move-exception
            java.lang.String r4 = "ResourcesFlusher"
            java.lang.String r5 = "Could not retrieve value from Resources#mDrawableCache"
            android.util.Log.e(r4, r5, r2)
            goto L_0x0027
        L_0x0043:
            r4 = 0
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.app.ResourcesFlusher.flushLollipops(android.content.res.Resources):boolean");
    }

    @RequiresApi(23)
    private static boolean flushMarshmallows(@NonNull Resources resources) {
        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = Resources.class.getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mDrawableCache field", e);
            }
            sDrawableCacheFieldFetched = true;
        }
        Object drawableCache = null;
        if (sDrawableCacheField != null) {
            try {
                drawableCache = sDrawableCacheField.get(resources);
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "Could not retrieve value from Resources#mDrawableCache", e2);
            }
        }
        if (drawableCache == null || drawableCache == null) {
            return false;
        }
        return flushThemedResourcesCache(drawableCache);
    }

    @RequiresApi(24)
    private static boolean flushNougats(@NonNull Resources resources) {
        if (!sResourcesImplFieldFetched) {
            try {
                sResourcesImplField = Resources.class.getDeclaredField("mResourcesImpl");
                sResourcesImplField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mResourcesImpl field", e);
            }
            sResourcesImplFieldFetched = true;
        }
        if (sResourcesImplField == null) {
            return false;
        }
        Object resourcesImpl = null;
        try {
            resourcesImpl = sResourcesImplField.get(resources);
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Could not retrieve value from Resources#mResourcesImpl", e2);
        }
        if (resourcesImpl == null) {
            return false;
        }
        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = resourcesImpl.getClass().getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e3) {
                Log.e(TAG, "Could not retrieve ResourcesImpl#mDrawableCache field", e3);
            }
            sDrawableCacheFieldFetched = true;
        }
        Object drawableCache = null;
        if (sDrawableCacheField != null) {
            try {
                drawableCache = sDrawableCacheField.get(resourcesImpl);
            } catch (IllegalAccessException e4) {
                Log.e(TAG, "Could not retrieve value from ResourcesImpl#mDrawableCache", e4);
            }
        }
        if (drawableCache != null) {
            return flushThemedResourcesCache(drawableCache);
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: android.util.LongSparseArray} */
    /* JADX WARNING: Multi-variable type inference failed */
    @android.support.annotation.RequiresApi(16)
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean flushThemedResourcesCache(@android.support.annotation.NonNull java.lang.Object r9) {
        /*
            r8 = 0
            r7 = 1
            boolean r5 = sThemedResourceCacheClazzFetched
            if (r5 != 0) goto L_0x0011
            java.lang.String r5 = "android.content.res.ThemedResourceCache"
            java.lang.Class r5 = java.lang.Class.forName(r5)     // Catch:{ ClassNotFoundException -> 0x0016 }
            sThemedResourceCacheClazz = r5     // Catch:{ ClassNotFoundException -> 0x0016 }
        L_0x000f:
            sThemedResourceCacheClazzFetched = r7
        L_0x0011:
            java.lang.Class r5 = sThemedResourceCacheClazz
            if (r5 != 0) goto L_0x0021
            return r8
        L_0x0016:
            r1 = move-exception
            java.lang.String r5 = "ResourcesFlusher"
            java.lang.String r6 = "Could not find ThemedResourceCache class"
            android.util.Log.e(r5, r6, r1)
            goto L_0x000f
        L_0x0021:
            boolean r5 = sThemedResourceCache_mUnthemedEntriesFieldFetched
            if (r5 != 0) goto L_0x0038
            java.lang.Class r5 = sThemedResourceCacheClazz     // Catch:{ NoSuchFieldException -> 0x003d }
            java.lang.String r6 = "mUnthemedEntries"
            java.lang.reflect.Field r5 = r5.getDeclaredField(r6)     // Catch:{ NoSuchFieldException -> 0x003d }
            sThemedResourceCache_mUnthemedEntriesField = r5     // Catch:{ NoSuchFieldException -> 0x003d }
            java.lang.reflect.Field r5 = sThemedResourceCache_mUnthemedEntriesField     // Catch:{ NoSuchFieldException -> 0x003d }
            r6 = 1
            r5.setAccessible(r6)     // Catch:{ NoSuchFieldException -> 0x003d }
        L_0x0036:
            sThemedResourceCache_mUnthemedEntriesFieldFetched = r7
        L_0x0038:
            java.lang.reflect.Field r5 = sThemedResourceCache_mUnthemedEntriesField
            if (r5 != 0) goto L_0x0048
            return r8
        L_0x003d:
            r3 = move-exception
            java.lang.String r5 = "ResourcesFlusher"
            java.lang.String r6 = "Could not retrieve ThemedResourceCache#mUnthemedEntries field"
            android.util.Log.e(r5, r6, r3)
            goto L_0x0036
        L_0x0048:
            r4 = 0
            java.lang.reflect.Field r5 = sThemedResourceCache_mUnthemedEntriesField     // Catch:{ IllegalAccessException -> 0x0059 }
            java.lang.Object r5 = r5.get(r9)     // Catch:{ IllegalAccessException -> 0x0059 }
            r0 = r5
            android.util.LongSparseArray r0 = (android.util.LongSparseArray) r0     // Catch:{ IllegalAccessException -> 0x0059 }
            r4 = r0
        L_0x0053:
            if (r4 == 0) goto L_0x0064
            r4.clear()
            return r7
        L_0x0059:
            r2 = move-exception
            java.lang.String r5 = "ResourcesFlusher"
            java.lang.String r6 = "Could not retrieve value from ThemedResourceCache#mUnthemedEntries"
            android.util.Log.e(r5, r6, r2)
            goto L_0x0053
        L_0x0064:
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.app.ResourcesFlusher.flushThemedResourcesCache(java.lang.Object):boolean");
    }
}
