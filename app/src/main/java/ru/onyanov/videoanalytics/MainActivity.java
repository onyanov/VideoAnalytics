package ru.onyanov.videoanalytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import ru.onyanov.videoanalytics.parse.ParseService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View button1 = findViewById(R.id.image1);
        View button2 = findViewById(R.id.image2);
        View button3 = findViewById(R.id.image3);
        View button4 = findViewById(R.id.image4);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);

        Log.d(TAG, "onCreate: start");




    }

    private void exportFrameService() {
        Intent intent = new Intent(this, ParseService.class);
        intent.putExtra(ParseService.FIELD_EXPORT, true);
        startService(intent);
    }

    private void clearFrameService() {
        Intent intent = new Intent(this, ParseService.class);
        intent.putExtra(ParseService.FIELD_CLEAR, true);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.start:
                clearFrameService();
                break;
            case R.id.image1:
                addFrame(R.drawable.frame1);
                break;
            case R.id.image2:
                addFrame(R.drawable.frame2);
                break;
            case R.id.image3:
                addFrame(R.drawable.frame3);
                break;
            case R.id.image4:
                addFrame(R.drawable.frame4);
                break;
            case R.id.stop:
                exportFrameService();
                break;

        }
    }

    private void addFrame(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        int[] pixels = getPixelsFromBitmap(bitmap);
        sendPixelsToParser(pixels);
    }

    private int[] getPixelsFromBitmap(Bitmap bitmap) {
        final int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        Log.d(TAG, "getPixelsFromBitmap: pixels size = " + pixels.length);
        return pixels;
    }

    private void sendPixelsToParser(int[] pixels) {
        int pixelsPerRequest = 1024; //Max data for Intent is 4Kb. Integer takes 4 bytes.
        int chunksCount = (pixels.length + pixelsPerRequest - 1) / pixelsPerRequest;
        //Log.d(TAG, "onCreate: (" + pixels.length + " + " + pixelsPerRequest + " - 1) / " + pixelsPerRequest + ") = " + chunksCount);

        for (int i = 0; i < chunksCount; i++) {
            int offset = i * pixelsPerRequest;
            int length = offset + pixelsPerRequest > pixels.length ? pixels.length - offset : pixelsPerRequest;
            int[] chunk = new int[length];
            System.arraycopy(pixels, offset, chunk, 0, length);

            Intent intent = new Intent(this, ParseService.class);
            intent.putExtra(ParseService.FIELD_PIXELS, chunk);
            startService(intent);
        }
    }

    /**
     * Broadcast receiver for receiving status updates from the IntentService
     */
    private class ParseStateReceiver extends BroadcastReceiver {

        private ParseStateReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            int counterParsed = intent.getIntExtra(Constants.DATA_COUNT_PARSED, 0);
            int counterAll = intent.getIntExtra(Constants.DATA_COUNT_ALL, 0);
            showProgress(counterParsed, counterAll);
        }
    }

    private void showProgress(int parsed, int all) {

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
