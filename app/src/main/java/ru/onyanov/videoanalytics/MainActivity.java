package ru.onyanov.videoanalytics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Bitmap icon;
    private ColorParser colorParser;
    private int numOfThreads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d(TAG, "onCreate: start");
        icon = BitmapFactory.decodeResource(getResources(), R.drawable.frame3);

        colorParser = new ColorParser();


        // 01. calculate in one thread. To long!
        // calcPlain();

        // 02. calculate in eight threads.
        // calcEightThreads();

        numOfThreads = Runtime.getRuntime().availableProcessors();
        calcExecutor();
    }

    private void calcExecutor() {
        ExecutorService exec = Executors.newFixedThreadPool(numOfThreads);
        try {
            for (int y = 0; y < icon.getHeight(); y++) {
                final int finalY = y;
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        int pixel;
                        float[] hsv = new float[3];
                        for (int x = 0; x < icon.getHeight(); x++) {
                            pixel = icon.getPixel(x, finalY);
                            Color.colorToHSV(pixel, hsv);
                            colorParser.addColor(hsv);
                        }
                    }
                });
            }
        } finally {
            exec.shutdown();
            try {
                exec.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "calcExecutor: " + Arrays.toString(colorParser.getData()));
    }

    private void calcEightThreads() {
        int rows = icon.getHeight();
        int increment = rows / 8;
        Thread[] threads = new Thread[8];
        for (int i = 0; i < 8; i++) {
            threads[i] = new Thread(new Worker(i * increment, (i + 1) * increment));
            threads[i].start();
        }
        for(int i = 0; i < 8; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "calcEightThreads: " + Arrays.toString(colorParser.getData()));
    }


    private void calcPlain() {
        int pixel;
        float[] hsv = new float[3];
        for (int y = 0; y < icon.getHeight(); y++) {
            for (int x = 0; x < icon.getHeight(); x++) {
                pixel = icon.getPixel(x, y);
                Color.colorToHSV(pixel, hsv);
                colorParser.addColor(hsv);
            }
        }
        Log.d(TAG, "calcPlain: " + Arrays.toString(colorParser.getData()));
    }

    public class Worker implements Runnable {
        final private int minIndex; // first index, inclusive
        final private int maxIndex; // last index, exclusive

        public Worker(int minIndex, int maxIndex) {
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
        }

        public void run() {
            int pixel;
            float[] hsv = new float[3];
            for(int y = minIndex; y < maxIndex; y++) {
                for (int x = 0; x < icon.getHeight(); x++) {
                    pixel = icon.getPixel(x, y);
                    Color.colorToHSV(pixel, hsv);
                    colorParser.addColor(hsv);
                }
            }
        }
    }
}
