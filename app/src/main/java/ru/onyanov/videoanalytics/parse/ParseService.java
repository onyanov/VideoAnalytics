package ru.onyanov.videoanalytics.parse;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.onyanov.videoanalytics.ColorPalette;
import ru.onyanov.videoanalytics.Constants;

public class ParseService extends Service implements ParseThreadPoolExecutor.ParseListener {

    private static final String TAG = "ParseService";
    public static final String FIELD_PIXELS = "pixels";
    public static final String FIELD_CLEAR = "clear";
    public static final String FIELD_EXPORT = "export";
    private ParseThreadPoolExecutor executor;
    private ColorPalette palette;
    private int counterParsed;
    private int counterAll;

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
                executor.awaitTermination(10, TimeUnit.SECONDS);
                Log.d(TAG, "onStartCommand: export " + palette);
                stopSelf();
            } catch (InterruptedException e) {
                stopSelf();
            }
        } else {
            int[] pixels = intent.getIntArrayExtra(FIELD_PIXELS);
            executor.execute(new ParseThread(pixels));
            counterAll++;
            notifyProgress();
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
        counterParsed++;
        notifyProgress();
    }

    /**
     * Sends broadcast to activity
     */
    private void notifyProgress() {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION_PARSE)
            .putExtra(Constants.DATA_COUNT_PARSED, counterParsed)
            .putExtra(Constants.DATA_COUNT_ALL, counterAll);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
