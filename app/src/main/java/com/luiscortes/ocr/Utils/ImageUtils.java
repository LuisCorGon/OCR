package com.luiscortes.ocr.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageUtils {
    // 🔹 Redimensionar Bitmap al tamaño que necesita YOLO
    public static Bitmap resizeBitmap(Bitmap original, int width, int height) {
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    // 🔹 Convertir Bitmap a ByteBuffer para TensorFlow Lite
    public static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int modelInputSize) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * modelInputSize * modelInputSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[modelInputSize * modelInputSize];
        bitmap.getPixels(intValues, 0, modelInputSize, 0, 0, modelInputSize, modelInputSize);

        int pixel = 0;
        for (int i = 0; i < modelInputSize; i++) {
            for (int j = 0; j < modelInputSize; j++) {
                int val = intValues[pixel++]; // RGB
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f); // Red
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);  // Green
                byteBuffer.putFloat((val & 0xFF) / 255.0f);         // Blue
            }
        }
        return byteBuffer;
    }

    // 🔹 Cargar una imagen desde recursos (para pruebas)
    public static Bitmap loadImageFromResources(android.content.res.Resources res, int drawableId) {
        return BitmapFactory.decodeResource(res, drawableId);
    }
}
