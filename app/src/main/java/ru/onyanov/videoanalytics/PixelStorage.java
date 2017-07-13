package ru.onyanov.videoanalytics;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by onaynov.dn on 13.07.2017.
 */

public class PixelStorage {

    private Deque<int[]> queue = new LinkedBlockingDeque<>();

    private static final PixelStorage ourInstance = new PixelStorage();

    public static PixelStorage getInstance() {
        return ourInstance;
    }

    private PixelStorage() {}

    public Deque<int[]> getQueue() {
        return queue;
    }
}
