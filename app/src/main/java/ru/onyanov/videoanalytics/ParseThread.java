package ru.onyanov.videoanalytics;

import android.graphics.Color;

/**
 * Created by onaynov.dn on 12.07.2017.
 */

public class ParseThread implements Runnable {

    private int[] pixels;
    private ColorPalette palette = new ColorPalette();

    public ParseThread(int[] pixels) {
        this.pixels = pixels;
    }

    public ColorPalette getPalette() {
        return palette;
    }

    @Override
    public void run() {
        for (int pixel : pixels) {
            parseColor(pixel);
        }
    }

    private void parseColor(int pixel) {
        float[] hsv = new float[3];
        Color.colorToHSV(pixel, hsv);

        if (hsv[1] < 0.1 && hsv[2] > 0.9) palette.white++;
        else if (hsv[2] < 0.1) palette.black++;
        else {
            float deg = hsv[0];
            if (deg >=  30 && deg <  70) palette.yellow++;
            else if (deg >=  70 && deg < 150) palette.green++;
            else if (deg >= 150 && deg < 200) palette.cyan++;
            else if (deg >= 210 && deg < 270) palette.blue++;
            else if (deg >= 270 && deg < 330) palette.magenta++;
            else palette.red++;
        }
    }

}
