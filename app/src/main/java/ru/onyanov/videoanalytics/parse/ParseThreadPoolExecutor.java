package ru.onyanov.videoanalytics.parse;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.onyanov.videoanalytics.ColorPalette;

/**
 * Allows parallel processing tasks and reports progress to listener
 */

class ParseThreadPoolExecutor extends ThreadPoolExecutor {

    private static final String TAG = "ParserThreadPoolExecuto";

    private static final int corePoolSize = 2;
    private static final int maxPoolSize = 128;
    private static final long keepAliveTime = 5;
    private final ParseListener listener;

    ParseThreadPoolExecutor(BlockingQueue<Runnable> workQueue, ParseListener listener) {
        super(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
        this.listener = listener;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            Log.e(TAG, "afterExecute: ", t);
            return;
        }
        ColorPalette palette = ((ParseThread) r).getPalette();
        listener.onParsed(palette);
    }

    interface ParseListener {

        /**
         * Called every time when a chunk was parsed. May be called multiple times during session.
         * @param palette Color palette of the chunk. Does not cover neither a frame nor whole video.
         */
        void onParsed(ColorPalette palette);
    }
}
