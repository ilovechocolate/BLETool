package com.nana.bletool;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {

    private ToastUtils() {
        throw new UnsupportedOperationException("cannot be instantiated!");
    }

    public static void showShort(Context context, String message) {
        checkNull(context);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showShort(Context context, int messageId) {
        checkNull(context);
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context, String message) {
        checkNull(context);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showLong(Context context, int messageId) {
        checkNull(context);
        Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
    }

    public static void centerShowShort(Context context, int imageId, String message) {
        checkNull(context);
        show(context, imageId, message, Toast.LENGTH_SHORT);
    }

    public static void centerShowShort(Context context, int imageId, int messageId) {
        checkNull(context);
        show(context, imageId, context.getString(messageId), Toast.LENGTH_SHORT);
    }

    public static void centerShowLong(Context context, int imageId, String message) {
        checkNull(context);
        show(context, imageId, message, Toast.LENGTH_LONG);
    }

    public static void centerShowLong(Context context, int imageId, int messageId) {
        checkNull(context);
        show(context, imageId, context.getString(messageId), Toast.LENGTH_LONG);
    }

    private static void show(Context context, int imageId, String message, int length) {
        View view = LayoutInflater.from(context).inflate(R.layout.toast, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.toast_image);
        imageView.setImageResource(imageId);
        TextView textView = (TextView) view.findViewById(R.id.toast_message);
        textView.setText(message);
        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(length);
        toast.show();
    }

    private static void checkNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException();
        }
    }

}