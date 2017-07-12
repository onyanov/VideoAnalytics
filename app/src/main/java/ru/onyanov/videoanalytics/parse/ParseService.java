package ru.onyanov.videoanalytics.parse;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.onyanov.videoanalytics.ColorPalette;

public class ParseService extends Service implements ParseThreadPoolExecutor.ParseListener {

    private static final String TAG = "ParseService";
    public static final String FIELD_PIXELS = "pixels";
    public static final String FIELD_CLEAR = "clear";
    public static final String FIELD_EXPORT = "export";
    private ParseThreadPoolExecutor executor;
    private ColorPalette palette;

    @Override
    public void onCreate() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(1024);

        executor = new ParseThreadPoolExecutor(blockingQueue, this);

        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                                          ThreadPoolExecutor executor) {
                System.out.println("DemoTask Rejected. Repeat after a second.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executor.execute(r);
            }
        });

        // Let start all core threads initially
        executor.prestartAllCoreThreads();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(FIELD_CLEAR, false)) {
            palette = new ColorPalette();
        } else if (intent.getBooleanExtra(FIELD_EXPORT, false)) {
            executor.shutdown();
            try {
                while(!executor.awaitTermination(10, TimeUnit.SECONDS));
                Log.d(TAG, "onStartCommand: export " + palette);
                stopSelf();
            } catch (InterruptedException e) {}
        } else {
            int[] pixels = intent.getIntArrayExtra(FIELD_PIXELS);
            executor.execute(new ParseThread(pixels));
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onParsed(ColorPalette chunkPalette) {
        palette.mergeWith(chunkPalette);
        Log.d(TAG, "onParsed: " + palette);
    }
}
