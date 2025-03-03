package com.luiscortes.ocr.Model;

import android.content.Context;
import android.graphics.Bitmap;

import com.luiscortes.ocr.Utils.ImageUtils;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class YOLODetector {

    private static final int MODEL_INPUT_SIZE = 32;


    public YOLODetector() {
        // Constructor
    }

    private Interpreter interpreter;

    public YOLODetector(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
    }

    private ByteBuffer loadModelFile(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open("best.tflite");
        ReadableByteChannel channel = Channels.newChannel(inputStream);
        ByteBuffer buffer = ByteBuffer.allocateDirect(inputStream.available());
        buffer.order(ByteOrder.nativeOrder());
        channel.read(buffer);
        buffer.rewind();
        return buffer;
    }

    public ByteBuffer prepareImage(Bitmap bitmap) {
        // 🔹 Redimensionamos la imagen
        Bitmap resizedBitmap = ImageUtils.resizeBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE);

        // 🔹 Convertimos a ByteBuffer
        return ImageUtils.convertBitmapToByteBuffer(resizedBitmap, MODEL_INPUT_SIZE);
    }

    public float[][] runInference(ByteBuffer inputBuffer) {
        // 🔹 Definir la salida del modelo
        // 1: Significa que la predicción es para una sola imagen.
        // 30: Es el número de "pasos" o "cajas" detectadas (posiblemente caracteres).
        // 21: Es el número de valores asociados a cada carácter.
        float[][][] outputArray = new float[1][30][21]; // Ajusta YOUR_OUTPUT_SIZE según Netron


        // 🔹 Ejecutar el modelo con la imagen preprocesada
        interpreter.run(inputBuffer, outputArray);

        return outputArray[0];
    }

    public String decodePredictions(float[][] outputArray) {
        // 🔹 Diccionario de caracteres (ajústalo según tu modelo)
        String[] charMap = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"}; // 21 elementos

        StringBuilder result = new StringBuilder();

        // 🔹 Recorrer las 30 predicciones
        for (float[] charProb : outputArray) {
            int maxIndex = 0;
            float maxProb = charProb[0];

            // 🔹 Encontrar el índice con la probabilidad más alta
            for (int i = 1; i < charProb.length; i++) {
                if (charProb[i] > maxProb) {
                    maxProb = charProb[i];
                    maxIndex = i;
                }
            }

            // 🔹 Agregar la letra correspondiente
            result.append(charMap[maxIndex]);
        }

        return result.toString().trim(); // 🔹 Retornar la frase sin espacios extra
    }


    public Interpreter getInterpreter() {
        return interpreter;
    }

}
