package com.example.yaksok;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class DrawerAlarmActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Dialog);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_alarm);
    }
}
