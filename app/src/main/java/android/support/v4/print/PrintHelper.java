package android.support.v4.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PrintHelper {
    public static final int COLOR_MODE_COLOR = 2;
    public static final int COLOR_MODE_MONOCHROME = 1;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int SCALE_MODE_FILL = 2;
    public static final int SCALE_MODE_FIT = 1;
    private final PrintHelperVersionImpl mImpl;

    @Retention(RetentionPolicy.SOURCE)
    private @interface ColorMode {
    }

    public interface OnPrintFinishCallback {
        void onFinish();
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface Orientation {
    }

    interface PrintHelperVersionImpl {
        int getColorMode();

        int getOrientation();

        int getScaleMode();

        void printBitmap(String str, Bitmap bitmap, OnPrintFinishCallback onPrintFinishCallback);

        void printBitmap(String str, Uri uri, OnPrintFinishCallback onPrintFinishCallback) throws FileNotFoundException;

        void setColorMode(int i);

        void setOrientation(int i);

        void setScaleMode(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface ScaleMode {
    }

    public static boolean systemSupportsPrint() {
        return Build.VERSION.SDK_INT >= 19;
    }

    private static final class PrintHelperStub implements PrintHelperVersionImpl {
        int mColorMode;
        int mOrientation;
        int mScaleMode;

        /* synthetic */ PrintHelperStub(PrintHelperStub printHelperStub) {
            this();
        }

        private PrintHelperStub() {
            this.mScaleMode = 2;
            this.mColorMode = 2;
            this.mOrientation = 1;
        }

        public void setScaleMode(int scaleMode) {
            this.mScaleMode = scaleMode;
        }

        public int getScaleMode() {
            return this.mScaleMode;
        }

        public int getColorMode() {
            return this.mColorMode;
        }

        public void setColorMode(int colorMode) {
            this.mColorMode = colorMode;
        }

        public void setOrientation(int orientation) {
            this.mOrientation = orientation;
        }

        public int getOrientation() {
            return this.mOrientation;
        }

        public void printBitmap(String jobName, Bitmap bitmap, OnPrintFinishCallback callback) {
        }

        public void printBitmap(String jobName, Uri imageFile, OnPrintFinishCallback callback) {
        }
    }

    @RequiresApi(19)
    private static class PrintHelperApi19 implements PrintHelperVersionImpl {
        private static final String LOG_TAG = "PrintHelperApi19";
        private static final int MAX_PRINT_SIZE = 3500;
        int mColorMode = 2;
        final Context mContext;
        BitmapFactory.Options mDecodeOptions = null;
        protected boolean mIsMinMarginsHandlingCorrect = true;
        /* access modifiers changed from: private */
        public final Object mLock = new Object();
        int mOrientation;
        protected boolean mPrintActivityRespectsOrientation = true;
        int mScaleMode = 2;

        PrintHelperApi19(Context context) {
            this.mContext = context;
        }

        public void setScaleMode(int scaleMode) {
            this.mScaleMode = scaleMode;
        }

        public int getScaleMode() {
            return this.mScaleMode;
        }

        public void setColorMode(int colorMode) {
            this.mColorMode = colorMode;
        }

        public void setOrientation(int orientation) {
            this.mOrientation = orientation;
        }

        public int getOrientation() {
            if (this.mOrientation == 0) {
                return 1;
            }
            return this.mOrientation;
        }

        public int getColorMode() {
            return this.mColorMode;
        }

        /* access modifiers changed from: private */
        public static boolean isPortrait(Bitmap bitmap) {
            return bitmap.getWidth() <= bitmap.getHeight();
        }

        /* access modifiers changed from: protected */
        public PrintAttributes.Builder copyAttributes(PrintAttributes other) {
            PrintAttributes.Builder b = new PrintAttributes.Builder().setMediaSize(other.getMediaSize()).setResolution(other.getResolution()).setMinMargins(other.getMinMargins());
            if (other.getColorMode() != 0) {
                b.setColorMode(other.getColorMode());
            }
            return b;
        }

        public void printBitmap(String jobName, Bitmap bitmap, OnPrintFinishCallback callback) {
            PrintAttributes.MediaSize mediaSize;
            if (bitmap != null) {
                final int fittingMode = this.mScaleMode;
                PrintManager printManager = (PrintManager) this.mContext.getSystemService("print");
                if (isPortrait(bitmap)) {
                    mediaSize = PrintAttributes.MediaSize.UNKNOWN_PORTRAIT;
                } else {
                    mediaSize = PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE;
                }
                final String str = jobName;
                final Bitmap bitmap2 = bitmap;
                final OnPrintFinishCallback onPrintFinishCallback = callback;
                printManager.print(jobName, new PrintDocumentAdapter() {
                    private PrintAttributes mAttributes;

                    public void onLayout(PrintAttributes oldPrintAttributes, PrintAttributes newPrintAttributes, CancellationSignal cancellationSignal, PrintDocumentAdapter.LayoutResultCallback layoutResultCallback, Bundle bundle) {
                        this.mAttributes = newPrintAttributes;
                        layoutResultCallback.onLayoutFinished(new PrintDocumentInfo.Builder(str).setContentType(1).setPageCount(1).build(), !newPrintAttributes.equals(oldPrintAttributes));
                    }

                    public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback writeResultCallback) {
                        PrintHelperApi19.this.writeBitmap(this.mAttributes, fittingMode, bitmap2, fileDescriptor, cancellationSignal, writeResultCallback);
                    }

                    public void onFinish() {
                        if (onPrintFinishCallback != null) {
                            onPrintFinishCallback.onFinish();
                        }
                    }
                }, new PrintAttributes.Builder().setMediaSize(mediaSize).setColorMode(this.mColorMode).build());
            }
        }

        /* access modifiers changed from: private */
        public Matrix getMatrix(int imageWidth, int imageHeight, RectF content, int fittingMode) {
            float scale;
            Matrix matrix = new Matrix();
            float scale2 = content.width() / ((float) imageWidth);
            if (fittingMode == 2) {
                scale = Math.max(scale2, content.height() / ((float) imageHeight));
            } else {
                scale = Math.min(scale2, content.height() / ((float) imageHeight));
            }
            matrix.postScale(scale, scale);
            matrix.postTranslate((content.width() - (((float) imageWidth) * scale)) / 2.0f, (content.height() - (((float) imageHeight) * scale)) / 2.0f);
            return matrix;
        }

        /* access modifiers changed from: private */
        public void writeBitmap(PrintAttributes attributes, int fittingMode, Bitmap bitmap, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback writeResultCallback) {
            final PrintAttributes pdfAttributes;
            if (this.mIsMinMarginsHandlingCorrect) {
                pdfAttributes = attributes;
            } else {
                pdfAttributes = copyAttributes(attributes).setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0)).build();
            }
            final CancellationSignal cancellationSignal2 = cancellationSignal;
            final Bitmap bitmap2 = bitmap;
            final ParcelFileDescriptor parcelFileDescriptor = fileDescriptor;
            final PrintAttributes printAttributes = attributes;
            final int i = fittingMode;
            final PrintDocumentAdapter.WriteResultCallback writeResultCallback2 = writeResultCallback;
            new AsyncTask<Void, Void, Throwable>() {
                /* access modifiers changed from: protected */
                /* JADX WARNING: Unknown top exception splitter block from list: {B:35:0x00b2=Splitter:B:35:0x00b2, B:50:0x00e5=Splitter:B:50:0x00e5, B:22:0x0079=Splitter:B:22:0x0079} */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public java.lang.Throwable doInBackground(java.lang.Void... r15) {
                    /*
                        r14 = this;
                        r13 = 0
                        android.os.CancellationSignal r9 = r2     // Catch:{ Throwable -> 0x00ba }
                        boolean r9 = r9.isCanceled()     // Catch:{ Throwable -> 0x00ba }
                        if (r9 == 0) goto L_0x000a
                        return r13
                    L_0x000a:
                        android.print.pdf.PrintedPdfDocument r7 = new android.print.pdf.PrintedPdfDocument     // Catch:{ Throwable -> 0x00ba }
                        android.support.v4.print.PrintHelper$PrintHelperApi19 r9 = android.support.v4.print.PrintHelper.PrintHelperApi19.this     // Catch:{ Throwable -> 0x00ba }
                        android.content.Context r9 = r9.mContext     // Catch:{ Throwable -> 0x00ba }
                        android.print.PrintAttributes r10 = r3     // Catch:{ Throwable -> 0x00ba }
                        r7.<init>(r9, r10)     // Catch:{ Throwable -> 0x00ba }
                        android.support.v4.print.PrintHelper$PrintHelperApi19 r9 = android.support.v4.print.PrintHelper.PrintHelperApi19.this     // Catch:{ Throwable -> 0x00ba }
                        android.graphics.Bitmap r10 = r4     // Catch:{ Throwable -> 0x00ba }
                        android.print.PrintAttributes r11 = r3     // Catch:{ Throwable -> 0x00ba }
                        int r11 = r11.getColorMode()     // Catch:{ Throwable -> 0x00ba }
                        android.graphics.Bitmap r5 = r9.convertBitmapForColorMode(r10, r11)     // Catch:{ Throwable -> 0x00ba }
                        android.os.CancellationSignal r9 = r2     // Catch:{ Throwable -> 0x00ba }
                        boolean r9 = r9.isCanceled()     // Catch:{ Throwable -> 0x00ba }
                        if (r9 == 0) goto L_0x002c
                        return r13
                    L_0x002c:
                        r9 = 1
                        android.graphics.pdf.PdfDocument$Page r6 = r7.startPage(r9)     // Catch:{ all -> 0x00a5 }
                        android.support.v4.print.PrintHelper$PrintHelperApi19 r9 = android.support.v4.print.PrintHelper.PrintHelperApi19.this     // Catch:{ all -> 0x00a5 }
                        boolean r9 = r9.mIsMinMarginsHandlingCorrect     // Catch:{ all -> 0x00a5 }
                        if (r9 == 0) goto L_0x0081
                        android.graphics.RectF r0 = new android.graphics.RectF     // Catch:{ all -> 0x00a5 }
                        android.graphics.pdf.PdfDocument$PageInfo r9 = r6.getInfo()     // Catch:{ all -> 0x00a5 }
                        android.graphics.Rect r9 = r9.getContentRect()     // Catch:{ all -> 0x00a5 }
                        r0.<init>(r9)     // Catch:{ all -> 0x00a5 }
                    L_0x0044:
                        android.support.v4.print.PrintHelper$PrintHelperApi19 r9 = android.support.v4.print.PrintHelper.PrintHelperApi19.this     // Catch:{ all -> 0x00a5 }
                        int r10 = r5.getWidth()     // Catch:{ all -> 0x00a5 }
                        int r11 = r5.getHeight()     // Catch:{ all -> 0x00a5 }
                        int r12 = r7     // Catch:{ all -> 0x00a5 }
                        android.graphics.Matrix r4 = r9.getMatrix(r10, r11, r0, r12)     // Catch:{ all -> 0x00a5 }
                        android.support.v4.print.PrintHelper$PrintHelperApi19 r9 = android.support.v4.print.PrintHelper.PrintHelperApi19.this     // Catch:{ all -> 0x00a5 }
                        boolean r9 = r9.mIsMinMarginsHandlingCorrect     // Catch:{ all -> 0x00a5 }
                        if (r9 == 0) goto L_0x00bc
                    L_0x005a:
                        android.graphics.Canvas r9 = r6.getCanvas()     // Catch:{ all -> 0x00a5 }
                        r10 = 0
                        r9.drawBitmap(r5, r4, r10)     // Catch:{ all -> 0x00a5 }
                        r7.finishPage(r6)     // Catch:{ all -> 0x00a5 }
                        android.os.CancellationSignal r9 = r2     // Catch:{ all -> 0x00a5 }
                        boolean r9 = r9.isCanceled()     // Catch:{ all -> 0x00a5 }
                        if (r9 == 0) goto L_0x00cb
                        r7.close()     // Catch:{ Throwable -> 0x00ba }
                        android.os.ParcelFileDescriptor r9 = r5     // Catch:{ Throwable -> 0x00ba }
                        if (r9 == 0) goto L_0x0079
                        android.os.ParcelFileDescriptor r9 = r5     // Catch:{ IOException -> 0x00f1 }
                        r9.close()     // Catch:{ IOException -> 0x00f1 }
                    L_0x0079:
                        android.graphics.Bitmap r9 = r4     // Catch:{ Throwable -> 0x00ba }
                        if (r5 == r9) goto L_0x0080
                        r5.recycle()     // Catch:{ Throwable -> 0x00ba }
                    L_0x0080:
                        return r13
                    L_0x0081:
                        android.print.pdf.PrintedPdfDocument r1 = new android.print.pdf.PrintedPdfDocument     // Catch:{ all -> 0x00a5 }
                        android.support.v4.print.PrintHelper$PrintHelperApi19 r9 = android.support.v4.print.PrintHelper.PrintHelperApi19.this     // Catch:{ all -> 0x00a5 }
                        android.content.Context r9 = r9.mContext     // Catch:{ all -> 0x00a5 }
                        android.print.PrintAttributes r10 = r6     // Catch:{ all -> 0x00a5 }
                        r1.<init>(r9, r10)     // Catch:{ all -> 0x00a5 }
                        r9 = 1
                        android.graphics.pdf.PdfDocument$Page r2 = r1.startPage(r9)     // Catch:{ all -> 0x00a5 }
                        android.graphics.RectF r0 = new android.graphics.RectF     // Catch:{ all -> 0x00a5 }
                        android.graphics.pdf.PdfDocument$PageInfo r9 = r2.getInfo()     // Catch:{ all -> 0x00a5 }
                        android.graphics.Rect r9 = r9.getContentRect()     // Catch:{ all -> 0x00a5 }
                        r0.<init>(r9)     // Catch:{ all -> 0x00a5 }
                        r1.finishPage(r2)     // Catch:{ all -> 0x00a5 }
                        r1.close()     // Catch:{ all -> 0x00a5 }
                        goto L_0x0044
                    L_0x00a5:
                        r9 = move-exception
                        r7.close()     // Catch:{ Throwable -> 0x00ba }
                        android.os.ParcelFileDescriptor r10 = r5     // Catch:{ Throwable -> 0x00ba }
                        if (r10 == 0) goto L_0x00b2
                        android.os.ParcelFileDescriptor r10 = r5     // Catch:{ IOException -> 0x00ed }
                        r10.close()     // Catch:{ IOException -> 0x00ed }
                    L_0x00b2:
                        android.graphics.Bitmap r10 = r4     // Catch:{ Throwable -> 0x00ba }
                        if (r5 == r10) goto L_0x00b9
                        r5.recycle()     // Catch:{ Throwable -> 0x00ba }
                    L_0x00b9:
                        throw r9     // Catch:{ Throwable -> 0x00ba }
                    L_0x00ba:
                        r8 = move-exception
                        return r8
                    L_0x00bc:
                        float r9 = r0.left     // Catch:{ all -> 0x00a5 }
                        float r10 = r0.top     // Catch:{ all -> 0x00a5 }
                        r4.postTranslate(r9, r10)     // Catch:{ all -> 0x00a5 }
                        android.graphics.Canvas r9 = r6.getCanvas()     // Catch:{ all -> 0x00a5 }
                        r9.clipRect(r0)     // Catch:{ all -> 0x00a5 }
                        goto L_0x005a
                    L_0x00cb:
                        java.io.FileOutputStream r9 = new java.io.FileOutputStream     // Catch:{ all -> 0x00a5 }
                        android.os.ParcelFileDescriptor r10 = r5     // Catch:{ all -> 0x00a5 }
                        java.io.FileDescriptor r10 = r10.getFileDescriptor()     // Catch:{ all -> 0x00a5 }
                        r9.<init>(r10)     // Catch:{ all -> 0x00a5 }
                        r7.writeTo(r9)     // Catch:{ all -> 0x00a5 }
                        r7.close()     // Catch:{ Throwable -> 0x00ba }
                        android.os.ParcelFileDescriptor r9 = r5     // Catch:{ Throwable -> 0x00ba }
                        if (r9 == 0) goto L_0x00e5
                        android.os.ParcelFileDescriptor r9 = r5     // Catch:{ IOException -> 0x00ef }
                        r9.close()     // Catch:{ IOException -> 0x00ef }
                    L_0x00e5:
                        android.graphics.Bitmap r9 = r4     // Catch:{ Throwable -> 0x00ba }
                        if (r5 == r9) goto L_0x00ec
                        r5.recycle()     // Catch:{ Throwable -> 0x00ba }
                    L_0x00ec:
                        return r13
                    L_0x00ed:
                        r3 = move-exception
                        goto L_0x00b2
                    L_0x00ef:
                        r3 = move-exception
                        goto L_0x00e5
                    L_0x00f1:
                        r3 = move-exception
                        goto L_0x0079
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.support.v4.print.PrintHelper.PrintHelperApi19.AnonymousClass2.doInBackground(java.lang.Void[]):java.lang.Throwable");
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Throwable throwable) {
                    if (cancellationSignal2.isCanceled()) {
                        writeResultCallback2.onWriteCancelled();
                    } else if (throwable == null) {
                        writeResultCallback2.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    } else {
                        Log.e(PrintHelperApi19.LOG_TAG, "Error writing printed content", throwable);
                        writeResultCallback2.onWriteFailed((CharSequence) null);
                    }
                }
            }.execute(new Void[0]);
        }

        public void printBitmap(String jobName, Uri imageFile, OnPrintFinishCallback callback) throws FileNotFoundException {
            final int fittingMode = this.mScaleMode;
            final String str = jobName;
            final OnPrintFinishCallback onPrintFinishCallback = callback;
            final Uri uri = imageFile;
            PrintDocumentAdapter printDocumentAdapter = new PrintDocumentAdapter() {
                /* access modifiers changed from: private */
                public PrintAttributes mAttributes;
                Bitmap mBitmap = null;
                AsyncTask<Uri, Boolean, Bitmap> mLoadBitmap;

                public void onLayout(PrintAttributes oldPrintAttributes, PrintAttributes newPrintAttributes, CancellationSignal cancellationSignal, PrintDocumentAdapter.LayoutResultCallback layoutResultCallback, Bundle bundle) {
                    synchronized (this) {
                        this.mAttributes = newPrintAttributes;
                    }
                    if (cancellationSignal.isCanceled()) {
                        layoutResultCallback.onLayoutCancelled();
                    } else if (this.mBitmap != null) {
                        layoutResultCallback.onLayoutFinished(new PrintDocumentInfo.Builder(str).setContentType(1).setPageCount(1).build(), !newPrintAttributes.equals(oldPrintAttributes));
                    } else {
                        final Uri uri = uri;
                        final String str = str;
                        final CancellationSignal cancellationSignal2 = cancellationSignal;
                        final PrintAttributes printAttributes = newPrintAttributes;
                        final PrintAttributes printAttributes2 = oldPrintAttributes;
                        final PrintDocumentAdapter.LayoutResultCallback layoutResultCallback2 = layoutResultCallback;
                        this.mLoadBitmap = new AsyncTask<Uri, Boolean, Bitmap>() {
                            /* access modifiers changed from: protected */
                            public void onPreExecute() {
                                cancellationSignal2.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                                    public void onCancel() {
                                        AnonymousClass3.this.cancelLoad();
                                        AnonymousClass1.this.cancel(false);
                                    }
                                });
                            }

                            /* access modifiers changed from: protected */
                            public Bitmap doInBackground(Uri... uris) {
                                try {
                                    return PrintHelperApi19.this.loadConstrainedBitmap(uri);
                                } catch (FileNotFoundException e) {
                                    return null;
                                }
                            }

                            /* access modifiers changed from: protected */
                            public void onPostExecute(Bitmap bitmap) {
                                PrintAttributes.MediaSize mediaSize;
                                super.onPostExecute(bitmap);
                                if (bitmap != null && (!PrintHelperApi19.this.mPrintActivityRespectsOrientation || PrintHelperApi19.this.mOrientation == 0)) {
                                    synchronized (this) {
                                        mediaSize = AnonymousClass3.this.mAttributes.getMediaSize();
                                    }
                                    if (!(mediaSize == null || mediaSize.isPortrait() == PrintHelperApi19.isPortrait(bitmap))) {
                                        Matrix rotation = new Matrix();
                                        rotation.postRotate(90.0f);
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotation, true);
                                    }
                                }
                                AnonymousClass3.this.mBitmap = bitmap;
                                if (bitmap != null) {
                                    layoutResultCallback2.onLayoutFinished(new PrintDocumentInfo.Builder(str).setContentType(1).setPageCount(1).build(), !printAttributes.equals(printAttributes2));
                                } else {
                                    layoutResultCallback2.onLayoutFailed((CharSequence) null);
                                }
                                AnonymousClass3.this.mLoadBitmap = null;
                            }

                            /* access modifiers changed from: protected */
                            public void onCancelled(Bitmap result) {
                                layoutResultCallback2.onLayoutCancelled();
                                AnonymousClass3.this.mLoadBitmap = null;
                            }
                        }.execute(new Uri[0]);
                    }
                }

                /* access modifiers changed from: private */
                public void cancelLoad() {
                    synchronized (PrintHelperApi19.this.mLock) {
                        if (PrintHelperApi19.this.mDecodeOptions != null) {
                            PrintHelperApi19.this.mDecodeOptions.requestCancelDecode();
                            PrintHelperApi19.this.mDecodeOptions = null;
                        }
                    }
                }

                public void onFinish() {
                    super.onFinish();
                    cancelLoad();
                    if (this.mLoadBitmap != null) {
                        this.mLoadBitmap.cancel(true);
                    }
                    if (onPrintFinishCallback != null) {
                        onPrintFinishCallback.onFinish();
                    }
                    if (this.mBitmap != null) {
                        this.mBitmap.recycle();
                        this.mBitmap = null;
                    }
                }

                public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback writeResultCallback) {
                    PrintHelperApi19.this.writeBitmap(this.mAttributes, fittingMode, this.mBitmap, fileDescriptor, cancellationSignal, writeResultCallback);
                }
            };
            PrintManager printManager = (PrintManager) this.mContext.getSystemService("print");
            PrintAttributes.Builder builder = new PrintAttributes.Builder();
            builder.setColorMode(this.mColorMode);
            if (this.mOrientation == 1 || this.mOrientation == 0) {
                builder.setMediaSize(PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE);
            } else if (this.mOrientation == 2) {
                builder.setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT);
            }
            printManager.print(jobName, printDocumentAdapter, builder.build());
        }

        /* access modifiers changed from: private */
        public Bitmap loadConstrainedBitmap(Uri uri) throws FileNotFoundException {
            BitmapFactory.Options decodeOptions;
            if (uri == null || this.mContext == null) {
                throw new IllegalArgumentException("bad argument to getScaledBitmap");
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            loadBitmap(uri, opt);
            int w = opt.outWidth;
            int h = opt.outHeight;
            if (w <= 0 || h <= 0) {
                return null;
            }
            int imageSide = Math.max(w, h);
            int sampleSize = 1;
            while (imageSide > MAX_PRINT_SIZE) {
                imageSide >>>= 1;
                sampleSize <<= 1;
            }
            if (sampleSize <= 0 || Math.min(w, h) / sampleSize <= 0) {
                return null;
            }
            synchronized (this.mLock) {
                this.mDecodeOptions = new BitmapFactory.Options();
                this.mDecodeOptions.inMutable = true;
                this.mDecodeOptions.inSampleSize = sampleSize;
                decodeOptions = this.mDecodeOptions;
            }
            try {
                Bitmap loadBitmap = loadBitmap(uri, decodeOptions);
                synchronized (this.mLock) {
                    this.mDecodeOptions = null;
                }
                return loadBitmap;
            } catch (Throwable th) {
                synchronized (this.mLock) {
                    this.mDecodeOptions = null;
                    throw th;
                }
            }
        }

        private Bitmap loadBitmap(Uri uri, BitmapFactory.Options o) throws FileNotFoundException {
            if (uri == null || this.mContext == null) {
                throw new IllegalArgumentException("bad argument to loadBitmap");
            }
            InputStream is = null;
            try {
                is = this.mContext.getContentResolver().openInputStream(uri);
                Bitmap decodeStream = BitmapFactory.decodeStream(is, (Rect) null, o);
                if (is != null) {
                    try {
                    } catch (IOException t) {
                        Log.w(LOG_TAG, "close fail ", t);
                    }
                }
                return decodeStream;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException t2) {
                        Log.w(LOG_TAG, "close fail ", t2);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public Bitmap convertBitmapForColorMode(Bitmap original, int colorMode) {
            if (colorMode != 1) {
                return original;
            }
            Bitmap grayscale = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(grayscale);
            Paint p = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.0f);
            p.setColorFilter(new ColorMatrixColorFilter(cm));
            c.drawBitmap(original, 0.0f, 0.0f, p);
            c.setBitmap((Bitmap) null);
            return grayscale;
        }
    }

    @RequiresApi(20)
    private static class PrintHelperApi20 extends PrintHelperApi19 {
        PrintHelperApi20(Context context) {
            super(context);
            this.mPrintActivityRespectsOrientation = false;
        }
    }

    @RequiresApi(23)
    private static class PrintHelperApi23 extends PrintHelperApi20 {
        /* access modifiers changed from: protected */
        public PrintAttributes.Builder copyAttributes(PrintAttributes other) {
            PrintAttributes.Builder b = super.copyAttributes(other);
            if (other.getDuplexMode() != 0) {
                b.setDuplexMode(other.getDuplexMode());
            }
            return b;
        }

        PrintHelperApi23(Context context) {
            super(context);
            this.mIsMinMarginsHandlingCorrect = false;
        }
    }

    @RequiresApi(24)
    private static class PrintHelperApi24 extends PrintHelperApi23 {
        PrintHelperApi24(Context context) {
            super(context);
            this.mIsMinMarginsHandlingCorrect = true;
            this.mPrintActivityRespectsOrientation = true;
        }
    }

    public PrintHelper(Context context) {
        if (Build.VERSION.SDK_INT >= 24) {
            this.mImpl = new PrintHelperApi24(context);
        } else if (Build.VERSION.SDK_INT >= 23) {
            this.mImpl = new PrintHelperApi23(context);
        } else if (Build.VERSION.SDK_INT >= 20) {
            this.mImpl = new PrintHelperApi20(context);
        } else if (Build.VERSION.SDK_INT >= 19) {
            this.mImpl = new PrintHelperApi19(context);
        } else {
            this.mImpl = new PrintHelperStub((PrintHelperStub) null);
        }
    }

    public void setScaleMode(int scaleMode) {
        this.mImpl.setScaleMode(scaleMode);
    }

    public int getScaleMode() {
        return this.mImpl.getScaleMode();
    }

    public void setColorMode(int colorMode) {
        this.mImpl.setColorMode(colorMode);
    }

    public int getColorMode() {
        return this.mImpl.getColorMode();
    }

    public void setOrientation(int orientation) {
        this.mImpl.setOrientation(orientation);
    }

    public int getOrientation() {
        return this.mImpl.getOrientation();
    }

    public void printBitmap(String jobName, Bitmap bitmap) {
        this.mImpl.printBitmap(jobName, bitmap, (OnPrintFinishCallback) null);
    }

    public void printBitmap(String jobName, Bitmap bitmap, OnPrintFinishCallback callback) {
        this.mImpl.printBitmap(jobName, bitmap, callback);
    }

    public void printBitmap(String jobName, Uri imageFile) throws FileNotFoundException {
        this.mImpl.printBitmap(jobName, imageFile, (OnPrintFinishCallback) null);
    }

    public void printBitmap(String jobName, Uri imageFile, OnPrintFinishCallback callback) throws FileNotFoundException {
        this.mImpl.printBitmap(jobName, imageFile, callback);
    }
}
