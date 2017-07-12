package ru.onyanov.videoanalytics;

/**
 * Object keep numbers of each color coinside.
 */

public class ColorPalette {

    public int white;

    public int red;

    public int yellow;

    public int green;

    public int cyan;

    public int blue;

    public int magenta;

    public int black;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("white: ").append(white).append(", ")
                .append("red: ").append(red).append(", ")
                .append("yellow: ").append(yellow).append(", ")
                .append("green: ").append(green).append(", ")
                .append("cyan: ").append(cyan).append(", ")
                .append("blue: ").append(blue).append(", ")
                .append("magenta: ").append(magenta).append(", ")
                .append("black: ").append(black);
        return sb.toString();
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

}
