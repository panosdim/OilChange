package com.intracom.padi.oilchange;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class MainActivity extends AppCompatActivity {
    private boolean isChronometerRunning;
    private Chronometer chrElapsedTime;
    private FloatingActionButton btnStartStop;
    private TextView txtUsedTime;
    private EditText txtTotalTime;
    private SharedPreferences settings;
    private CircularProgressBar prgUsedTime;
    private SharedPreferences.Editor editor;
    private static final long TOTAL_TIME = 180000;
    private static final String TAG = "PADI_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getPreferences(0);
        editor = settings.edit();

        btnStartStop = (FloatingActionButton) findViewById(R.id.btnStartStop);
        FloatingActionButton btnReset = (FloatingActionButton) findViewById(R.id.btnReset);
        FloatingActionButton btnAddTime = (FloatingActionButton) findViewById(R.id.btnAddTime);
        chrElapsedTime = (Chronometer) findViewById(R.id.chrElapsedTime);
        txtTotalTime = (EditText) findViewById(R.id.txtTotalTime);
        txtUsedTime = (TextView) findViewById(R.id.txtUsedTime);
        prgUsedTime = (CircularProgressBar) findViewById(R.id.prgUsedTime);

        isChronometerRunning = false;
        updateTimes();

        txtTotalTime.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            String s = txtTotalTime.getText().toString();
                            if (s.matches("\\d+:\\d+:\\d+||\\d+:\\d+")) {
                                editor.putLong("total", convertStrTimeToLong(s));
                                editor.apply();
                            }
                            updateTimes();
                        }
                        return false;
                    }
                });

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isChronometerRunning) {
                    long elapsedTime = (SystemClock.elapsedRealtime() - chrElapsedTime.getBase()) / 1000;
                    long totElapsedTime = settings.getLong("elapsed", 0);
                    editor.putLong("elapsed", elapsedTime + totElapsedTime);
                    editor.apply();
                    updateTimes();

                    chrElapsedTime.stop();
                    chrElapsedTime.setBase(SystemClock.elapsedRealtime());
                    isChronometerRunning = false;
                    btnStartStop.setImageResource(R.drawable.ic_play);
                } else {
                    chrElapsedTime.setBase(SystemClock.elapsedRealtime());
                    chrElapsedTime.start();
                    isChronometerRunning = true;
                    btnStartStop.setImageResource(R.drawable.ic_stop);
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Reset Used Time");
                alertDialogBuilder
                        .setMessage("Are you sure you want to reset Used Time")
                        .setCancelable(true)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                editor.putLong("elapsed", 0);
                                editor.apply();
                                updateTimes();
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        btnAddTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // Get the layout inflater
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.add_time, null);

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(dialogView)
                        .setTitle("Add Used Time")
                        .setCancelable(true)
                        .setPositiveButton("Save",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                EditText txtAddTime = (EditText) dialogView.findViewById(R.id.txtAddTime);
                                String s = txtAddTime.getText().toString();
                                if (s.matches("\\d+:\\d+:\\d+||\\d+:\\d+")) {
                                    long totElapsedTime = settings.getLong("elapsed", 0);
                                    totElapsedTime += convertStrTimeToLong(s);
                                    editor.putLong("elapsed", totElapsedTime);
                                    editor.apply();
                                }
                                updateTimes();
                            }
                        })
                        .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isChronometerRunning) {
            long elapsedTime = (SystemClock.elapsedRealtime() - chrElapsedTime.getBase()) / 1000;
            long totElapsedTime = settings.getLong("elapsed", 0);
            editor.putLong("elapsed", elapsedTime + totElapsedTime);
            editor.apply();
        }
    }

    /**
     * Convert a String representing time to long
     *
     * @param strTime String representing time in HH:MM:SS or MM:SS format
     * @return The long number of seconds
     */
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

    /**
     * Update the UI elements with needed changes
     */
    protected void updateTimes() {
        long tot = settings.getLong("total", TOTAL_TIME);
        long elapsed = settings.getLong("elapsed", 0);
        float percent = (elapsed * 100.0f) / tot;
        txtUsedTime.setText(String.format("%s - %s", "Used Time", DateUtils.formatElapsedTime(elapsed)));
        txtTotalTime.setText(DateUtils.formatElapsedTime(tot));
        if (percent < 60.0) {
            prgUsedTime.setColor(ContextCompat.getColor(this, R.color.green));
            prgUsedTime.setBackgroundColor(ContextCompat.getColor(this, R.color.background_green));
        } else if (percent >= 60.0 && percent < 80.0) {
            prgUsedTime.setColor(ContextCompat.getColor(this, R.color.orange));
            prgUsedTime.setBackgroundColor(ContextCompat.getColor(this, R.color.background_orange));
        } else {
            prgUsedTime.setColor(ContextCompat.getColor(this, R.color.red));
            prgUsedTime.setBackgroundColor(ContextCompat.getColor(this, R.color.background_red));
        }
        prgUsedTime.setProgress(percent);
    }
}
