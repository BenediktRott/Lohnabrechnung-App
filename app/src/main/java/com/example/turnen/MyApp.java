package com.example.turnen;

import android.app.Application;
import android.os.StrictMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends Application {
    public static ExecutorService executorService = Executors.newFixedThreadPool(1);

    public MyApp() {
        /*
        if(BuildConfig.DEBUG)
            StrictMode.enableDefaults();
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());

         */
    }

}
