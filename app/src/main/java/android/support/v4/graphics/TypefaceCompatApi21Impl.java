package android.support.v4.graphics;

import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;

@RequiresApi(21)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
class TypefaceCompatApi21Impl extends TypefaceCompatBaseImpl {
    private static final String TAG = "TypefaceCompatApi21Impl";

    TypefaceCompatApi21Impl() {
    }

    private File getFile(ParcelFileDescriptor fd) {
        try {
            String path = Os.readlink("/proc/self/fd/" + fd.getFd());
            if (OsConstants.S_ISREG(Os.stat(path).st_mode)) {
                return new File(path);
            }
            return null;
        } catch (ErrnoException e) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004b, code lost:
        r13 = r10;
        r10 = r9;
        r9 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        throw r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0057, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0074, code lost:
        r9 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0075, code lost:
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0092, code lost:
        r11 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0093, code lost:
        if (r10 == null) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0095, code lost:
        r10 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0097, code lost:
        if (r10 != r11) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0099, code lost:
        r10.addSuppressed(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x009d, code lost:
        throw r9;
     */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0050 A[SYNTHETIC, Splitter:B:27:0x0050] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0055 A[SYNTHETIC, Splitter:B:30:0x0055] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x006e A[SYNTHETIC, Splitter:B:49:0x006e] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0073 A[SYNTHETIC, Splitter:B:52:0x0073] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0074 A[Catch:{ all -> 0x0074, all -> 0x004a }, ExcHandler: all (th java.lang.Throwable), PHI: r7 
      PHI: (r7v1 'pfd' android.os.ParcelFileDescriptor) = (r7v0 'pfd' android.os.ParcelFileDescriptor), (r7v4 'pfd' android.os.ParcelFileDescriptor), (r7v4 'pfd' android.os.ParcelFileDescriptor), (r7v4 'pfd' android.os.ParcelFileDescriptor), (r7v4 'pfd' android.os.ParcelFileDescriptor), (r7v4 'pfd' android.os.ParcelFileDescriptor), (r7v4 'pfd' android.os.ParcelFileDescriptor), (r7v4 'pfd' android.os.ParcelFileDescriptor) binds: [B:4:0x0016, B:52:0x0073, B:49:0x006e, B:50:?, B:19:0x0047, B:20:?, B:16:0x0042, B:17:?] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0082 A[Catch:{ all -> 0x0074, all -> 0x004a }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x009d A[Catch:{ IOException -> 0x0056 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.graphics.Typeface createFromFontInfo(android.content.Context r15, android.os.CancellationSignal r16, @android.support.annotation.NonNull android.support.v4.provider.FontsContractCompat.FontInfo[] r17, int r18) {
        /*
            r14 = this;
            r0 = r17
            int r9 = r0.length
            r10 = 1
            if (r9 >= r10) goto L_0x0008
            r9 = 0
            return r9
        L_0x0008:
            r0 = r17
            r1 = r18
            android.support.v4.provider.FontsContractCompat$FontInfo r2 = r14.findBestInfo(r0, r1)
            android.content.ContentResolver r8 = r15.getContentResolver()
            r11 = 0
            r7 = 0
            android.net.Uri r9 = r2.getUri()     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
            java.lang.String r10 = "r"
            r0 = r16
            android.os.ParcelFileDescriptor r7 = r8.openFileDescriptor(r9, r10, r0)     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
            java.io.File r4 = r14.getFile(r7)     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
            if (r4 == 0) goto L_0x0031
            boolean r9 = r4.canRead()     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
            r9 = r9 ^ 1
            if (r9 == 0) goto L_0x0083
        L_0x0031:
            r10 = 0
            r5 = 0
            java.io.FileInputStream r6 = new java.io.FileInputStream     // Catch:{ Throwable -> 0x0066, all -> 0x009e }
            java.io.FileDescriptor r9 = r7.getFileDescriptor()     // Catch:{ Throwable -> 0x0066, all -> 0x009e }
            r6.<init>(r9)     // Catch:{ Throwable -> 0x0066, all -> 0x009e }
            android.graphics.Typeface r9 = super.createFromInputStream(r15, r6)     // Catch:{ Throwable -> 0x00a3, all -> 0x00a0 }
            if (r6 == 0) goto L_0x0045
            r6.close()     // Catch:{ Throwable -> 0x0059, all -> 0x0074 }
        L_0x0045:
            if (r10 == 0) goto L_0x005b
            throw r10     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
        L_0x0048:
            r9 = move-exception
            throw r9     // Catch:{ all -> 0x004a }
        L_0x004a:
            r10 = move-exception
            r13 = r10
            r10 = r9
            r9 = r13
        L_0x004e:
            if (r7 == 0) goto L_0x0053
            r7.close()     // Catch:{ Throwable -> 0x0092 }
        L_0x0053:
            if (r10 == 0) goto L_0x009d
            throw r10     // Catch:{ IOException -> 0x0056 }
        L_0x0056:
            r3 = move-exception
            r9 = 0
            return r9
        L_0x0059:
            r10 = move-exception
            goto L_0x0045
        L_0x005b:
            if (r7 == 0) goto L_0x0060
            r7.close()     // Catch:{ Throwable -> 0x0063 }
        L_0x0060:
            if (r11 == 0) goto L_0x0065
            throw r11     // Catch:{ IOException -> 0x0056 }
        L_0x0063:
            r11 = move-exception
            goto L_0x0060
        L_0x0065:
            return r9
        L_0x0066:
            r9 = move-exception
        L_0x0067:
            throw r9     // Catch:{ all -> 0x0068 }
        L_0x0068:
            r10 = move-exception
            r13 = r10
            r10 = r9
            r9 = r13
        L_0x006c:
            if (r5 == 0) goto L_0x0071
            r5.close()     // Catch:{ Throwable -> 0x0077, all -> 0x0074 }
        L_0x0071:
            if (r10 == 0) goto L_0x0082
            throw r10     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
        L_0x0074:
            r9 = move-exception
            r10 = r11
            goto L_0x004e
        L_0x0077:
            r12 = move-exception
            if (r10 != 0) goto L_0x007c
            r10 = r12
            goto L_0x0071
        L_0x007c:
            if (r10 == r12) goto L_0x0071
            r10.addSuppressed(r12)     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
            goto L_0x0071
        L_0x0082:
            throw r9     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
        L_0x0083:
            android.graphics.Typeface r9 = android.graphics.Typeface.createFromFile(r4)     // Catch:{ Throwable -> 0x0048, all -> 0x0074 }
            if (r7 == 0) goto L_0x008c
            r7.close()     // Catch:{ Throwable -> 0x008f }
        L_0x008c:
            if (r11 == 0) goto L_0x0091
            throw r11     // Catch:{ IOException -> 0x0056 }
        L_0x008f:
            r11 = move-exception
            goto L_0x008c
        L_0x0091:
            return r9
        L_0x0092:
            r11 = move-exception
            if (r10 != 0) goto L_0x0097
            r10 = r11
            goto L_0x0053
        L_0x0097:
            if (r10 == r11) goto L_0x0053
            r10.addSuppressed(r11)     // Catch:{ IOException -> 0x0056 }
            goto L_0x0053
        L_0x009d:
            throw r9     // Catch:{ IOException -> 0x0056 }
        L_0x009e:
            r9 = move-exception
            goto L_0x006c
        L_0x00a0:
            r9 = move-exception
            r5 = r6
            goto L_0x006c
        L_0x00a3:
            r9 = move-exception
            r5 = r6
            goto L_0x0067
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatApi21Impl.createFromFontInfo(android.content.Context, android.os.CancellationSignal, android.support.v4.provider.FontsContractCompat$FontInfo[], int):android.graphics.Typeface");
    }
}
