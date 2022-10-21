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

    ImageView iv; //439 balanin lab2 threads
    SeekBar sb_core,sb_threads;
    TextView tv_core_value, tv_thread_value, tv_process;
    Switch sw_default, sw_gaussian;


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
        int k = sb_core.getProgress();
        int s = h / n;

        Thread[] t = new Thread[n];
        long startTime = System.currentTimeMillis();
        if (sw_gaussian.isActivated())
        {

            double sigma = k/2;

            int kernelWidth = (2 * k) +1;
            Double[][] kernel = new Double[kernelWidth][kernelWidth];
            double sum = 0;
            for (int x=-k; x < k; x++) {
                for (int y = -k; y < k; y++) {

                    double exponentNumerator = (double)(-(x * x + y * y));
                    double exponentDenominator = (2 * sigma * sigma);

                    double eExpression = Math.pow(Math.E, exponentNumerator / exponentDenominator);
                    double kernelValue = (eExpression / (2 * Math.PI * sigma * sigma));

                    // We add radius to the indices to prevent out of bound issues because x and y can be negative
                    kernel[x + k][y + k] = kernelValue;
                    sum += kernelValue;
                }
            }

            // Normalize the kernel
            // This ensures that all of the values in the kernel together add up to 1
            for (int x=0; x < kernelWidth; x++){
                for (int y=0; y < kernelWidth; y++){
                    kernel[x][y] /= sum;
                }
            }

            GaussianWorker[] worker = new GaussianWorker[n];
            for (int i = 0; i < t.length; i++) {
                worker[i] = new GaussianWorker();
                worker[i].bmp = bmp;
                worker[i].res = res;
                worker[i].kernel = kernel;
                worker[i].w = w;
                worker[i].h = h;
                worker[i].k = k;
                worker[i].y0 = s * i;
                worker[i].y1 = worker[i].y0 + s;

                t[i] = new Thread(worker[i]);
                t[i].start();
            }
        }
        else {
            Worker[] worker = new Worker[n];
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

    class GaussianWorker implements Runnable {

        public int y0, y1;
        public int w, h;
        public int k;
        public Double[][] kernel;
        public Bitmap bmp, res;

        @Override
        public void run() {

            for (int y = y0; y < y1; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    int sumRed = 0;
                    int sumGreen = 0;
                    int sumBlue = 0;


                    for (int kernelX =-k; kernelX < k; kernelX ++){
                        for (int kernelY = -k; kernelY < k;kernelY ++){

                            double kernelValue = kernel[kernelX + k][kernelY + k];

                            int c = bmp.getPixel(x-kernelX,y-kernelY);
                            sumRed += Color.red(c) * kernelValue;
                            sumGreen += Color.green(c) * kernelValue;
                            sumBlue += Color.blue(c) * kernelValue;
                        }
                    }
                    res.setPixel(x, y, Color.rgb(sumRed, sumGreen, sumBlue));

                }
            }
        }
    }


}