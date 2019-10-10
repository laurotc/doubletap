package com.example.doubletap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.widget.Toast;
//import android.util.Log;

public class SensorActivity extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor proximity;
    private long firstTap = 0;
    private long secondTap = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public final void onCreate() {
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        if (!powerManager.isScreenOn()) {
            float proximity = event.values[0];

            if (proximity >= 5) {
                if (this.firstTap == 0) {
                    this.firstTap = SystemClock.elapsedRealtime();
                }
            }
            else {
                if (this.firstTap > 0 && this.secondTap == 0) {
                    this.secondTap = SystemClock.elapsedRealtime();
                }

                long tapDiff = this.secondTap - this.firstTap;

                if (tapDiff > 0 && tapDiff <= 1000) {
                    //Log.d("wakeup screen", String.valueOf(tapDiff));
                    WakeLock wakeLock = powerManager.newWakeLock(
                            PowerManager.FULL_WAKE_LOCK
                                    | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                            "app::wakeup");
                    wakeLock.acquire();
                    wakeLock.release();

                    this.firstTap = 0;
                    this.secondTap = 0;
                }
            }
        }
    }

    @Override
    public void onDestroy() {//here u should unregister sensor
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        this.sensorManager.unregisterListener(this);
    }

    @Override//here u should register sensor and write onStartCommand not onStart
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        this.sensorManager.registerListener(this, this.proximity, SensorManager.SENSOR_DELAY_FASTEST);

        return Service.START_STICKY;
    }
}
