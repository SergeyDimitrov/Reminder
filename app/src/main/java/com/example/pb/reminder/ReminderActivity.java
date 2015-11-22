package com.example.pb.reminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReminderActivity extends AppCompatActivity {

    private SharedPreferences settings;

    private EditText titleView;
    private EditText dateView;
    private EditText timeView;
    private EditText descriptionView;
    private Button saveButton;

    private static final int DATE_DIALOG_ID = 1;
    private static final int TIME_DIALOG_ID = 2;

    private int day, month, year, hour, minute;

    private boolean isDataOk() {
        if (descriptionView.getText().toString().equals("") || titleView.getText().toString().equals("")
                || timeView.getText().toString().equals("") || dateView.getText().toString().equals("")) return false;
        else return true;
    }

    private long saveData() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ReminderReceiver.APP_PREFERENCES_TITLE, titleView.getText().toString());
        editor.putString(ReminderReceiver.APP_PREFERENCES_DESCRIPTION, descriptionView.getText().toString());
        long time = 0;
        try {
            time = DateFormat.getDateTimeInstance().parse(dateView.getText().toString()
                    + " " + timeView.getText().toString()).getTime();
            editor.putLong(ReminderReceiver.APP_PREFERENCES_TIME, time);
        } catch (Exception e) {
            Log.d("myTAG", "Bad parsing");
        }
        editor.commit();
        return time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("myTAG", "onCreate");
        setContentView(R.layout.activity_reminder);

        saveButton = (Button)findViewById(R.id.save_button);
        descriptionView = (EditText)findViewById(R.id.description_view);
        titleView = (EditText)findViewById(R.id.title_view);
        dateView = (EditText)findViewById(R.id.date_view);
        timeView = (EditText)findViewById(R.id.time_view);

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                showDialog(DATE_DIALOG_ID);
            }
        });

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR);
                minute = c.get(Calendar.MINUTE);
                showDialog(TIME_DIALOG_ID);
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataOk()) {
                    ReminderReceiver.setAlarm(ReminderActivity.this, saveData());
                    Toast.makeText(ReminderActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReminderActivity.this, R.string.no_data_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        settings = getSharedPreferences(ReminderReceiver.APP_PREFERENCES, MODE_PRIVATE);

        if (settings.contains(ReminderReceiver.APP_PREFERENCES_TITLE)) {
            titleView.setText(settings.getString(ReminderReceiver.APP_PREFERENCES_TITLE, ""));
            descriptionView.setText(settings.getString(ReminderReceiver.APP_PREFERENCES_DESCRIPTION, ""));
            long time = settings.getLong(ReminderReceiver.APP_PREFERENCES_TIME, 0);
            Date date = new Date(time);
            dateView.setText(DateFormat.getDateInstance().format(date));
            timeView.setText(DateFormat.getTimeInstance().format(date));
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, monthOfYear);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateView.setText(DateFormat.getDateInstance().format(new Date(c.getTimeInMillis())));
                    }
                }, year, month, day);
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        c.set(Calendar.SECOND, 0);
                        timeView.setText(DateFormat.getTimeInstance().format(new Date(c.getTimeInMillis())));
                    }
                }, hour, minute, true);
        }
        return null;
    }
    
    public static void enableReceiver(boolean enable, Context context) {
        int flag = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        ComponentName componentName = new ComponentName(context, ReminderReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(componentName, flag, PackageManager.DONT_KILL_APP);
        Log.d("myTAG", "Receiver enabled = " + enable);
    }

}
