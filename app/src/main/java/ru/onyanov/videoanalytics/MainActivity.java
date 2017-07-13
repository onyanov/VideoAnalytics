package ru.onyanov.videoanalytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

import ru.onyanov.videoanalytics.parse.ParseService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private TextView progress;
    private BarChart result;
    private ParseStateReceiver reciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View start = findViewById(R.id.start);
        View stop = findViewById(R.id.stop);
        View button1 = findViewById(R.id.image1);
        View button2 = findViewById(R.id.image2);
        View button3 = findViewById(R.id.image3);
        View button4 = findViewById(R.id.image4);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);

        progress = (TextView) findViewById(R.id.progress);
        result = (BarChart) findViewById(R.id.result);

        reciever = new ParseStateReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(reciever,
                new IntentFilter(Constants.BROADCAST_ACTION_PARSE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reciever);
        super.onPause();
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
        int pixelsPerRequest = 10240; //Max data for Intent is 4Kb. Integer takes 4 bytes.
        int chunksCount = (pixels.length + pixelsPerRequest - 1) / pixelsPerRequest;
        //Log.d(TAG, "onCreate: (" + pixels.length + " + " + pixelsPerRequest + " - 1) / " + pixelsPerRequest + ") = " + chunksCount);

        for (int i = 0; i < chunksCount; i++) {
            int offset = i * pixelsPerRequest;
            int length = offset + pixelsPerRequest > pixels.length ? pixels.length - offset : pixelsPerRequest;
            int[] chunk = new int[length];
            System.arraycopy(pixels, offset, chunk, 0, length);
            PixelStorage.getInstance().getQueue().add(chunk);
        }
    }

    /**
     * Broadcast receiver for receiving status updates from the IntentService
     */
    private class ParseStateReceiver extends BroadcastReceiver {

        private ParseStateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ColorPalette palette = intent.getParcelableExtra(Constants.DATA_RESULT);
            if (palette != null) {
                showResult(palette);
            } else {
                int counterParsed = intent.getIntExtra(Constants.DATA_COUNT_PARSED, 0);
                int counterAll = intent.getIntExtra(Constants.DATA_COUNT_ALL, 0);
                showProgress(counterParsed, counterAll);
            }
        }
    }

    private void showResult(ColorPalette palette) {
        Log.d(TAG, "showResult: " + palette.toString());

        result.getDescription().setEnabled(false);

        // scaling can now only be done on x- and y-axis separately
        result.setPinchZoom(false);

        result.setDrawBarShadow(false);
        result.setDrawGridBackground(true);

        XAxis xAxis = result.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        result.getAxisLeft().setDrawGridLines(true);

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        palette.normalize();
        yVals1.add(new BarEntry(0, palette.white));
        yVals1.add(new BarEntry(1, palette.red));
        yVals1.add(new BarEntry(2, palette.yellow));
        yVals1.add(new BarEntry(3, palette.green));
        yVals1.add(new BarEntry(4, palette.cyan));
        yVals1.add(new BarEntry(5, palette.blue));
        yVals1.add(new BarEntry(6, palette.magenta));
        yVals1.add(new BarEntry(7, palette.black));

        BarDataSet set1;
        set1 = new BarDataSet(yVals1, "Data Set");
        set1.setColors(ColorPalette.DISPLAY_COLORS);
        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        result.setData(data);
        result.setFitBars(true);

        result.invalidate();

    }

    private void showProgress(int parsed, int all) {
        //progress.setText(getString(R.string.progress, parsed, all));
    }
}
