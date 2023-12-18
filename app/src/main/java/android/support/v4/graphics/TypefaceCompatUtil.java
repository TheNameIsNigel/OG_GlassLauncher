package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class TypefaceCompatUtil {
    private static final String CACHE_FILE_PREFIX = ".font";
    private static final String TAG = "TypefaceCompatUtil";

    private TypefaceCompatUtil() {
    }

    @Nullable
    public static File getTempFile(Context context) {
        String prefix = CACHE_FILE_PREFIX + Process.myPid() + "-" + Process.myTid() + "-";
        int i = 0;
        while (i < 100) {
            File file = new File(context.getCacheDir(), prefix + i);
            try {
                if (file.createNewFile()) {
                    return file;
                }
                i++;
            } catch (IOException e) {
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x002e A[SYNTHETIC, Splitter:B:23:0x002e] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0033 A[SYNTHETIC, Splitter:B:26:0x0033] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0041 A[Catch:{ IOException -> 0x0034 }] */
    @android.support.annotation.Nullable
    @android.support.annotation.RequiresApi(19)
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.nio.ByteBuffer mmap(java.io.File r11) {
        /*
            r9 = 0
            r7 = 0
            java.io.FileInputStream r8 = new java.io.FileInputStream     // Catch:{ Throwable -> 0x0026, all -> 0x0042 }
            r8.<init>(r11)     // Catch:{ Throwable -> 0x0026, all -> 0x0042 }
            java.nio.channels.FileChannel r0 = r8.getChannel()     // Catch:{ Throwable -> 0x0049, all -> 0x0045 }
            long r4 = r0.size()     // Catch:{ Throwable -> 0x0049, all -> 0x0045 }
            java.nio.channels.FileChannel$MapMode r1 = java.nio.channels.FileChannel.MapMode.READ_ONLY     // Catch:{ Throwable -> 0x0049, all -> 0x0045 }
            r2 = 0
            java.nio.MappedByteBuffer r2 = r0.map(r1, r2, r4)     // Catch:{ Throwable -> 0x0049, all -> 0x0045 }
            if (r8 == 0) goto L_0x001c
            r8.close()     // Catch:{ Throwable -> 0x0023 }
        L_0x001c:
            r1 = r9
        L_0x001d:
            if (r1 == 0) goto L_0x0025
            throw r1     // Catch:{ IOException -> 0x0020 }
        L_0x0020:
            r6 = move-exception
            r7 = r8
        L_0x0022:
            return r9
        L_0x0023:
            r1 = move-exception
            goto L_0x001d
        L_0x0025:
            return r2
        L_0x0026:
            r1 = move-exception
        L_0x0027:
            throw r1     // Catch:{ all -> 0x0028 }
        L_0x0028:
            r2 = move-exception
            r10 = r2
            r2 = r1
            r1 = r10
        L_0x002c:
            if (r7 == 0) goto L_0x0031
            r7.close()     // Catch:{ Throwable -> 0x0036 }
        L_0x0031:
            if (r2 == 0) goto L_0x0041
            throw r2     // Catch:{ IOException -> 0x0034 }
        L_0x0034:
            r6 = move-exception
            goto L_0x0022
        L_0x0036:
            r3 = move-exception
            if (r2 != 0) goto L_0x003b
            r2 = r3
            goto L_0x0031
        L_0x003b:
            if (r2 == r3) goto L_0x0031
            r2.addSuppressed(r3)     // Catch:{ IOException -> 0x0034 }
            goto L_0x0031
        L_0x0041:
            throw r1     // Catch:{ IOException -> 0x0034 }
        L_0x0042:
            r1 = move-exception
            r2 = r9
            goto L_0x002c
        L_0x0045:
            r1 = move-exception
            r7 = r8
            r2 = r9
            goto L_0x002c
        L_0x0049:
            r1 = move-exception
            r7 = r8
            goto L_0x0027
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(java.io.File):java.nio.ByteBuffer");
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x004e A[SYNTHETIC, Splitter:B:33:0x004e] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0054 A[SYNTHETIC, Splitter:B:37:0x0054] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005a A[SYNTHETIC, Splitter:B:41:0x005a] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0071 A[Catch:{ IOException -> 0x005b }] */
    @android.support.annotation.Nullable
    @android.support.annotation.RequiresApi(19)
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.nio.ByteBuffer mmap(android.content.Context r13, android.os.CancellationSignal r14, android.net.Uri r15) {
        /*
            r11 = 0
            android.content.ContentResolver r10 = r13.getContentResolver()
            r9 = 0
            r7 = 0
            java.lang.String r1 = "r"
            android.os.ParcelFileDescriptor r9 = r10.openFileDescriptor(r15, r1, r14)     // Catch:{ Throwable -> 0x0046, all -> 0x0072 }
            java.io.FileInputStream r8 = new java.io.FileInputStream     // Catch:{ Throwable -> 0x0046, all -> 0x0072 }
            java.io.FileDescriptor r1 = r9.getFileDescriptor()     // Catch:{ Throwable -> 0x0046, all -> 0x0072 }
            r8.<init>(r1)     // Catch:{ Throwable -> 0x0046, all -> 0x0072 }
            java.nio.channels.FileChannel r0 = r8.getChannel()     // Catch:{ Throwable -> 0x0079, all -> 0x0075 }
            long r4 = r0.size()     // Catch:{ Throwable -> 0x0079, all -> 0x0075 }
            java.nio.channels.FileChannel$MapMode r1 = java.nio.channels.FileChannel.MapMode.READ_ONLY     // Catch:{ Throwable -> 0x0079, all -> 0x0075 }
            r2 = 0
            java.nio.MappedByteBuffer r3 = r0.map(r1, r2, r4)     // Catch:{ Throwable -> 0x0079, all -> 0x0075 }
            if (r8 == 0) goto L_0x002c
            r8.close()     // Catch:{ Throwable -> 0x0039 }
        L_0x002c:
            r2 = r11
        L_0x002d:
            if (r9 == 0) goto L_0x0032
            r9.close()     // Catch:{ Throwable -> 0x003b }
        L_0x0032:
            r1 = r2
        L_0x0033:
            if (r1 == 0) goto L_0x0045
            throw r1     // Catch:{ IOException -> 0x0036 }
        L_0x0036:
            r6 = move-exception
            r7 = r8
        L_0x0038:
            return r11
        L_0x0039:
            r2 = move-exception
            goto L_0x002d
        L_0x003b:
            r1 = move-exception
            if (r2 == 0) goto L_0x0033
            if (r2 == r1) goto L_0x0032
            r2.addSuppressed(r1)     // Catch:{ IOException -> 0x0036 }
            r1 = r2
            goto L_0x0033
        L_0x0045:
            return r3
        L_0x0046:
            r1 = move-exception
        L_0x0047:
            throw r1     // Catch:{ all -> 0x0048 }
        L_0x0048:
            r2 = move-exception
            r12 = r2
            r2 = r1
            r1 = r12
        L_0x004c:
            if (r7 == 0) goto L_0x0051
            r7.close()     // Catch:{ Throwable -> 0x005d }
        L_0x0051:
            r3 = r2
        L_0x0052:
            if (r9 == 0) goto L_0x0057
            r9.close()     // Catch:{ Throwable -> 0x0067 }
        L_0x0057:
            r2 = r3
        L_0x0058:
            if (r2 == 0) goto L_0x0071
            throw r2     // Catch:{ IOException -> 0x005b }
        L_0x005b:
            r6 = move-exception
            goto L_0x0038
        L_0x005d:
            r3 = move-exception
            if (r2 == 0) goto L_0x0052
            if (r2 == r3) goto L_0x0051
            r2.addSuppressed(r3)     // Catch:{ IOException -> 0x005b }
            r3 = r2
            goto L_0x0052
        L_0x0067:
            r2 = move-exception
            if (r3 == 0) goto L_0x0058
            if (r3 == r2) goto L_0x0057
            r3.addSuppressed(r2)     // Catch:{ IOException -> 0x005b }
            r2 = r3
            goto L_0x0058
        L_0x0071:
            throw r1     // Catch:{ IOException -> 0x005b }
        L_0x0072:
            r1 = move-exception
            r2 = r11
            goto L_0x004c
        L_0x0075:
            r1 = move-exception
            r7 = r8
            r2 = r11
            goto L_0x004c
        L_0x0079:
            r1 = move-exception
            r7 = r8
            goto L_0x0047
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(android.content.Context, android.os.CancellationSignal, android.net.Uri):java.nio.ByteBuffer");
    }

    @Nullable
    @RequiresApi(19)
    public static ByteBuffer copyToDirectBuffer(Context context, Resources res, int id) {
        File tmpFile = getTempFile(context);
        if (tmpFile == null) {
            return null;
        }
        try {
            if (!copyToFile(tmpFile, res, id)) {
                return null;
            }
            ByteBuffer mmap = mmap(tmpFile);
            tmpFile.delete();
            return mmap;
        } finally {
            tmpFile.delete();
        }
    }

    public static boolean copyToFile(File file, InputStream is) {
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream os = new FileOutputStream(file, false);
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int readLen = is.read(buffer);
                    if (readLen != -1) {
                        os.write(buffer, 0, readLen);
                    } else {
                        closeQuietly(os);
                        return true;
                    }
                }
            } catch (IOException e) {
                e = e;
                fileOutputStream = os;
                try {
                    Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
                    closeQuietly(fileOutputStream);
                    return false;
                } catch (Throwable th) {
                    th = th;
                    closeQuietly(fileOutputStream);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = os;
                closeQuietly(fileOutputStream);
                throw th;
            }
        } catch (IOException e2) {
            e = e2;
            Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
            closeQuietly(fileOutputStream);
            return false;
        }
    }

    public static boolean copyToFile(File file, Resources res, int id) {
        InputStream is = null;
        try {
            is = res.openRawResource(id);
            return copyToFile(file, is);
        } finally {
            closeQuietly(is);
        }
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }
}
