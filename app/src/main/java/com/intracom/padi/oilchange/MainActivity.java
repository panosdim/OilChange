package com.intracom.padi.oilchange;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class MainActivity extends AppCompatActivity {
    private boolean isChronometerRunning;
    private Chronometer timer;
    private FloatingActionButton btnTimer;
    private TextView totElapsed;
    private SharedPreferences settings;
    private CircularProgressBar prgBar;
    private SharedPreferences.Editor editor;
    private static final String TAG = "PADI_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getPreferences(0);
        editor = settings.edit();

//        editor.putString("total", "50:00:00");
//        editor.apply();

        btnTimer = (FloatingActionButton) findViewById(R.id.btnStartStop);
        timer = (Chronometer) findViewById(R.id.timer);
        EditText total = (EditText) findViewById(R.id.txtTotal);
        totElapsed = (TextView) findViewById(R.id.totElapsedTime);
        prgBar = (CircularProgressBar) findViewById(R.id.progressBar);

        isChronometerRunning = false;
        total.setText(settings.getString("total", "50:00:00"));
        updateTimes();

        total.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().matches("\\d+:\\d+:\\d+")) {
                    editor.putString("total", s.toString());
                    editor.apply();

                    updateTimes();
                }
            }
        });

        btnTimer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isChronometerRunning) {
                    long elapsedTime = (SystemClock.elapsedRealtime() - timer.getBase()) / 1000;
                    long totElapsedTime = settings.getLong("elapsed", 0);
                    editor.putLong("elapsed", elapsedTime + totElapsedTime);
                    editor.apply();
                    updateTimes();

                    timer.stop();
                    timer.setBase(SystemClock.elapsedRealtime());
                    isChronometerRunning = false;
                    btnTimer.setImageResource(R.drawable.ic_play);
                } else {
                    timer.setBase(SystemClock.elapsedRealtime());
                    timer.start();
                    isChronometerRunning = true;
                    btnTimer.setImageResource(R.drawable.ic_stop);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putBoolean("silentMode", mSilentMode);
//
//        // Commit the edits!
//        editor.commit();
    }

    /**
     * Convert the time of the String type to long, such as:12:01:08
     * strTime String @param type of time
     * long@return type of time
     **/
    protected long convertStrTimeToLong(String strTime) {
        String[] timeArr = strTime.split(":");
        long longTime = (long) 0;
        if (timeArr.length == 2) {// if time is MM:SS format
            longTime = Integer.parseInt(timeArr[0]) * 60 + Integer.parseInt(timeArr[1]);
        } else if (timeArr.length == 3) {// if time is HH:MM:SS format
            longTime = Integer.parseInt(timeArr[0]) * 60 * 60 + Integer.parseInt(timeArr[1])
                    * 60 + Integer.parseInt(timeArr[2]);
        }
        return longTime;
    }

    protected void updateTimes() {
        long tot = convertStrTimeToLong(settings.getString("total", "50:00:00"));
        long elapsed = settings.getLong("elapsed", 0);
        float percent = (elapsed * 100.0f) / tot;
        totElapsed.setText(String.format("%s - %s", "Used Time", DateUtils.formatElapsedTime(elapsed)));
        prgBar.setProgress(percent);
        Log.d(TAG, "percent " + percent);
        Log.d(TAG, "elapsed " + elapsed);
        Log.d(TAG, "tot " + tot);
    }
}
