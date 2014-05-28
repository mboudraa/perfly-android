package com.samantha.app.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public final class ImageUtils {


    private ImageUtils() {
    }

    public static byte[] drawableToBytes(Drawable drawable) {
        return bitmapToByteArray(drawableToBitmap(drawable));
    }

    public static String drawableToBase64(Drawable drawable) {
        Bitmap bmp = drawableToBitmap(drawable);
        try {
            return bitmapToBase64(bmp);
        } finally {
            bmp.recycle();
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                                            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static String bitmapToBase64(Bitmap bmp) {
        byte[] byteArray = bitmapToByteArray(bmp);
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encoded;
    }
}
