package ru.onyanov.videoanalytics;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d(TAG, "onCreate: start");
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.frame3);


        //colorParser = new ColorParser(new ColorPalette());


        // 01. calculate in one thread. To long!
        // calcPlain();

        // 02. calculate in eight threads.
        // calcEightThreads();


        //calcExecutor();

        clearFrameService();

        final int[] pixels = new int[icon.getWidth() * icon.getHeight()];
        icon.getPixels(pixels, 0, icon.getWidth(), 0, 0, icon.getWidth(), icon.getHeight());

        Log.d(TAG, "onCreate: pixels size = " + pixels.length);

        int pixelsPerRequest = 1024;
        int chunksCount = (pixels.length + pixelsPerRequest - 1) / pixelsPerRequest;
        Log.d(TAG, "onCreate: (" + pixels.length + " + " + pixelsPerRequest + " - 1) / " + pixelsPerRequest + ") = " + chunksCount);

        for (int i = 0; i < chunksCount; i++) {
            int offset = i * pixelsPerRequest;
            int length = offset + pixelsPerRequest > pixels.length ? pixels.length - offset : pixelsPerRequest;
            int[] chunk = new int[length];
            System.arraycopy(pixels, offset, chunk, 0, length);

            Log.d(TAG, "send " + length + " pixels");

            Intent intent = new Intent(this, FrameService.class);
            intent.putExtra(FrameService.FIELD_PIXELS, chunk);
            startService(intent);
        }

        exportFrameService();
    }

    private void exportFrameService() {
        Intent intent = new Intent(this, FrameService.class);
        intent.putExtra(FrameService.FIELD_EXPORT, true);
        startService(intent);
    }

    private void clearFrameService() {
        Intent intent = new Intent(this, FrameService.class);
        intent.putExtra(FrameService.FIELD_CLEAR, true);
        startService(intent);
    }

    /*
    private void calcExecutor() {
        numOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(numOfThreads);

        final int[] pixels = new int[icon.getWidth() * icon.getHeight()];
        icon.getPixels(pixels, 0, icon.getWidth(), 0, 0, icon.getWidth(), icon.getHeight());

        final int chunkSize = pixels.length / numOfThreads;

        try {
            for (int t = 0; t < numOfThreads; t++) {
                final int thread = t;
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = chunkSize * thread; i < chunkSize * (thread + 1); i++) {
                            colorParser.addColor(pixels[i]);
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
        Log.d(TAG, "calcExecutor: " + colorParser.getPalette());
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
        Log.d(TAG, "calcEightThreads: " + colorParser.getPalette());
    }


    private void calcPlain() {
        int pixel;
        for (int y = 0; y < icon.getHeight(); y++) {
            for (int x = 0; x < icon.getHeight(); x++) {
                pixel = icon.getPixel(x, y);
                colorParser.addColor(pixel);
            }
        }
        Log.d(TAG, "calcPlain: " + colorParser.getPalette());
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
            for(int y = minIndex; y < maxIndex; y++) {
                for (int x = 0; x < icon.getHeight(); x++) {
                    pixel = icon.getPixel(x, y);
                    colorParser.addColor(pixel);
                }
            }
        }
    }
    */
}
