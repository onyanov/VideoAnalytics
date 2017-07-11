package ru.onyanov.videoanalytics;

/**
 * Created by onaynov.dn on 10.07.2017.
 */

public class ColorParser {

    private static final String TAG = "ColorParser";

    private static final int WHITE = 0;
    private static final int RED = 1;
    private static final int YELLOW = 2;
    private static final int GREEN = 3;
    private static final int CYAN = 4;
    private static final int BLUE = 5;
    private static final int MAGENTA = 6;
    private static final int BLACK = 7;

    private int[] data = {0, 0, 0, 0, 0, 0, 0, 0};

    public ColorParser() {

    }

    public void addColor(float[] hsv) {
        if (hsv[1] < 0.1 && hsv[2] > 0.9) increase(WHITE);
        else if (hsv[2] < 0.1) increase(BLACK);
        else {
            float deg = hsv[0];
            if      (deg >=   0 && deg <  30) increase(RED);
            else if (deg >=  30 && deg <  70) increase(YELLOW);
            else if (deg >=  70 && deg < 150) increase(GREEN);
            else if (deg >= 150 && deg < 200) increase(CYAN);
            else if (deg >= 210 && deg < 270) increase(BLUE);
            else if (deg >= 270 && deg < 330) increase(MAGENTA);
            else increase(RED);
        }

    }

    private void increase(int colorCode) {
        //Log.d(TAG, "addColor: " + colorCode);
        data[colorCode]++;
    }

    public int[] getData() {
        return data;
    }
}
