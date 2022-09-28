package com.example.a493balanin_lab_threads;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    public double sko = 5;
    ImageView iv;
    SeekBar sb_core,sb_threads;
    TextView tv_core_value, tv_thread_value, tv_process;
    Switch sw_default, sw_gaussian;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.iv);
        sb_core = findViewById(R.id.sb_core);
        sb_threads = findViewById(R.id.sb_threads);
        sb_threads.setProgress(1);
        sb_core.setProgress(3);
        tv_core_value = findViewById(R.id.tv_core_value);
        tv_thread_value = findViewById(R.id.tv_threads_value);
        tv_process = findViewById(R.id.tv_process);
        sw_default = findViewById(R.id.sw_default);
        sw_gaussian = findViewById(R.id.sw_gaussian);

        sb_core.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (sb_core.getProgress() <3)
                    sb_core.setProgress(3);
                tv_core_value.setText(String.valueOf(sb_core.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        sb_threads.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (sb_threads.getProgress() <1)
                    sb_threads.setProgress(1);
                tv_thread_value.setText(String.valueOf(sb_threads.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    public void myFunction(View view)
    {
        tv_process.setText("Processing...");
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.cat);
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Bitmap res = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        int n = sb_threads.getProgress();

        Thread[] t = new Thread[n];
        Worker[] worker = new Worker[n];
        int s = h / n;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < t.length; i++) {
            worker[i] = new Worker();
            worker[i].bmp = bmp;
            worker[i].res = res;
            worker[i].w = w;
            worker[i].h = h;
            worker[i].k = sb_core.getProgress();
            worker[i].y0 = s * i;
            worker[i].y1 = worker[i].y0 + s;

            t[i] = new Thread(worker[i]);
            t[i].start();
        }

        for (int i = 0; i < n; i++) {
            try {  t[i].join(); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        iv.setImageBitmap(res);
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        tv_process.setText("Processing complete!\nIt took " + (double)processingTime / 1000.0 + " seconds");

    }

    class Worker implements Runnable {

        public int y0, y1;
        public int w, h;
        public int k;
        public Bitmap bmp, res;

        @Override
        public void run() {

            Log.e("TEST","yo="+y0 +"\ny1=" + y1);
            for (int y = y0; y < y1; y++) {
                for (int x = 0; x < w; x++) {

                    int red = 0;
                    int green = 0;
                    int blue = 0;

                    for (int v = 0; v < k; v++) {
                        for (int u = 0; u < k; u++) {
                            int px = u + x - k / 2;
                            int py = v + y - k / 2;

                            if (px < 0) px = 0;
                            if (py < 0) py = 0;
                            if (px >= w) px = w - 1;
                            if (py >= h) py = h - 1;

                            int c = bmp.getPixel(px, py);
                            red += Color.red(c);
                            green += Color.green(c);
                            blue += Color.blue(c);
                        }
                    }
                    red /= k * k;
                    green /= k * k;
                    blue /= k * k;
                    res.setPixel(x, y, Color.rgb(red, green, blue));
                }
            }
        }
    }
/*
    class GaussianWorker implements Runnable {

        public int y0, y1;
        public int w, h;
        public int k;
        public Bitmap bmp, res;

        @Override
        public void run() {

            Log.e("TEST","yo="+y0 +"\ny1=" + y1);

            double sigma = 5.5 ;
            double sumMatrix=0;

            double[][] coeffMatrix = new double[k][k];
            double e;
            double g1;
            double temp_x, temp_y;
            int median = k/2;
            for (int i = 0; i < k; i++)
            {
                for (int j = 0; j < k; j++)
                {
                        g1 = 1.0D / (2 * Math.PI * (sigma*sigma));
                        temp_x = Math.pow((i-median), 2);
                        temp_y = Math.pow((j-median), 2);
                        e = Math.pow(Math.E, ((temp_x + temp_y) / (2 * (sigma*sigma))) / -1.0D);
                        coeffMatrix[i][j] = g1 * e;
                        sumMatrix += coeffMatrix[i][j];
                }
            }

            for (int y = y0; y < y1; y++)
            {
                for (int x = 0; x < w; x++)
                {

                    int sumRed = 0;
                    int sumGreen = 0;
                    int sumBlue = 0;

                    for (int v = 0; v < k; v++)
                    {
                        for (int u = 0; u < k; u++)
                        {

                            int px = u + x - k / 2;
                            int py = v + y - k / 2;

                            if (px < 0) px = 0;
                            if (py < 0) py = 0;
                            if (px >= w) px = w - 1;
                            if (py >= h) py = h - 1;

                            int c = bmp.getPixel(px, py);

                            sumRed += (double)Color.red(c) * coeffMatrix[u][v];
                            sumGreen += (double)Color.green(c) * coeffMatrix[u][v];
                            sumBlue += (double)Color.blue(c) * coeffMatrix[u][v];

                        }
                    }
                    sumRed/=sumMatrix;
                    sumGreen/=sumMatrix;
                    sumBlue/=sumMatrix;

                    res.setPixel(x, y, Color.rgb(sumRed, sumGreen, sumBlue));
                    //if (Thread.currentThread().isInterrupted()) return;
                }
            }
        }
    }

 */
}