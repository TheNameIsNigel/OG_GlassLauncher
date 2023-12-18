package com.google.android.glass.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;

public class ImageUtils {
    private static final float GRADIENT_RATIO = 0.5f;

    public static GradientDrawable getGradientDrawableFromPalette(Context context, Palette palette) {
        int dominantColor = palette.getDominantColor(ContextCompat.getColor(context, 17170444));
        int lighter = ColorUtils.blendARGB(dominantColor, -1, GRADIENT_RATIO);
        int darker = ColorUtils.blendARGB(dominantColor, ViewCompat.MEASURED_STATE_MASK, GRADIENT_RATIO);
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{lighter, darker});
    }

    public static Drawable getIconDrawable(Context context, Drawable drawable) {
        LayerDrawable layerDrawable = new LayerDrawable(getIconLayers(context, drawable));
        Bitmap bitmap = Bitmap.createBitmap(layerDrawable.getIntrinsicWidth(), layerDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        layerDrawable.draw(canvas);
        return new BitmapDrawable(context.getResources(), getCroppedBitmapCircle(bitmap));
    }

    private static Drawable[] getIconLayers(Context context, Drawable drawable) {
        if (drawable instanceof AdaptiveIconDrawable) {
            Drawable background = ((AdaptiveIconDrawable) drawable).getBackground();
            Drawable foreground = ((AdaptiveIconDrawable) drawable).getForeground();
            if (background == null || foreground == null) {
                return getDefaultLayers(context);
            }
            return new Drawable[]{background, foreground};
        } else if (drawable == null) {
            return getDefaultLayers(context);
        } else {
            return new Drawable[]{ContextCompat.getDrawable(context, 2131165331), drawable};
        }
    }

    private static Drawable[] getDefaultLayers(Context context) {
        return new Drawable[]{ContextCompat.getDrawable(context, 2131165298), ContextCompat.getDrawable(context, 2131165300)};
    }

    private static Bitmap getCroppedBitmapCircle(Bitmap bitmap) {
        float size = ((float) bitmap.getWidth()) / 2.0f;
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawCircle(size, size, size, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
