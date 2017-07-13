package ru.onyanov.videoanalytics.parse;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import ru.onyanov.videoanalytics.ColorPalette;
import ru.onyanov.videoanalytics.Constants;
import ru.onyanov.videoanalytics.PixelStorage;

public class ParseService extends Service implements ParseThreadPoolExecutor.ParseListener {

    private static final String TAG = "ParseService";
    public static final String FIELD_PIXELS = "pixels";
    public static final String FIELD_CLEAR = "clear";
    public static final String FIELD_EXPORT = "export";
    private ParseThreadPoolExecutor executor;
    private ColorPalette palette;
    private int counterParsed;
    private int counterAll;
    private Thread looperThread;
    private boolean isStopped;

    @Override
    public void onCreate() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(1024);

        executor = new ParseThreadPoolExecutor(blockingQueue, this);

        looperThread = new Thread(new LooperRunnable());
        looperThread.start();
    }

    private class LooperRunnable implements Runnable {


        @Override
        public void run() {
            while (!isStopped) {
                if (palette != null) {
                    int[] pixels = PixelStorage.getInstance().getQueue().poll();
                    if (pixels != null) {
                        Log.d(TAG, "run: " + pixels.length);
                        executor.execute(new ParseThread(pixels));
                        counterAll++;
                        notifyProgress();
                    }
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(FIELD_CLEAR, false)) {
            palette = new ColorPalette();
        } else if (intent.getBooleanExtra(FIELD_EXPORT, false)) {
            executor.shutdown();
            try {
                isStopped = true;
                executor.awaitTermination(10, TimeUnit.SECONDS);
                if (palette != null) {
                    Log.d(TAG, "onStartCommand: export " + palette);
                    notifyResult(palette);
                }
                stopSelf();
            } catch (InterruptedException e) {
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void notifyResult(ColorPalette palette) {
        notifyProgress();
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION_PARSE)
                .putExtra(Constants.DATA_RESULT, palette);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
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
       /* Intent localIntent = new Intent(Constants.BROADCAST_ACTION_PARSE)
                .putExtra(Constants.DATA_COUNT_PARSED, counterParsed)
                .putExtra(Constants.DATA_COUNT_ALL, counterAll);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        */
    }

}
