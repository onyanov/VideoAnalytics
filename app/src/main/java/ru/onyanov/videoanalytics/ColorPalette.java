package ru.onyanov.videoanalytics;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object keep numbers of each color coinside.
 */
public class ColorPalette implements Parcelable {

    public int white;

    public int red;

    public int yellow;

    public int green;

    public int cyan;

    public int blue;

    public int magenta;

    public int black;

    public static final int[] DISPLAY_COLORS = {
            Color.rgb(255, 255, 255),
            Color.rgb(244, 67, 54),
            Color.rgb(255, 235, 59),
            Color.rgb(76, 175, 80),
            Color.rgb(0, 188, 212),
            Color.rgb(33, 150, 243),
            Color.rgb(156, 39, 176),
            Color.rgb(0, 0, 0)
    };

    public ColorPalette() {
    }

    @Override
    public String toString() {
        return "white: " + white + ", " +
                "red: " + red + ", " +
                "yellow: " + yellow + ", " +
                "green: " + green + ", " +
                "cyan: " + cyan + ", " +
                "blue: " + blue + ", " +
                "magenta: " + magenta + ", " +
                "black: " + black;
    }

    public synchronized void mergeWith(ColorPalette palette) {
        white += palette.white;
        red += palette.red;
        yellow += palette.yellow;
        green += palette.green;
        cyan += palette.cyan;
        blue += palette.blue;
        magenta += palette.magenta;
        black += palette.black;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(white);
        dest.writeInt(red);
        dest.writeInt(yellow);
        dest.writeInt(green);
        dest.writeInt(cyan);
        dest.writeInt(blue);
        dest.writeInt(magenta);
        dest.writeInt(black);
    }

    public static final Parcelable.Creator<ColorPalette> CREATOR
            = new Parcelable.Creator<ColorPalette>() {
        public ColorPalette createFromParcel(Parcel in) {
            return new ColorPalette(in);
        }

        public ColorPalette[] newArray(int size) {
            return new ColorPalette[size];
        }
    };

    private ColorPalette(Parcel in) {
        white = in.readInt();
        red = in.readInt();
        yellow = in.readInt();
        green = in.readInt();
        cyan = in.readInt();
        blue = in.readInt();
        magenta = in.readInt();
        black = in.readInt();
    }

    public void normalize() {
        int sum = white + red + yellow + green + cyan + blue + magenta + black;
        if (sum == 0) return;

        white = white / (sum / 100);
        red = red / (sum / 100);
        yellow = yellow / (sum / 100);
        green = green / (sum / 100);
        cyan = cyan / (sum / 100);
        blue = blue / (sum / 100);
        magenta = magenta / (sum / 100);
        black = black / (sum / 100);
    }
}
