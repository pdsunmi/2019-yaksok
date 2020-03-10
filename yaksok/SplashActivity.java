package com.example.yaksok;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.auth.Session;
import com.kakao.kakaolink.v2.KakaoLinkCallback;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    String TAG = "SplashActivity";
    String documentid;
    private  static int time=3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Session.getCurrentSession().isOpened()){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent gintent = getIntent();
                    if(gintent.getAction() == Intent.ACTION_VIEW){
                        //Log.d(TAG, "action_view, with session!");
                        documentid = gintent.getDataString().substring(82);

                        //Log.d(TAG, "gintent documentid: " + documentid);
                    }
                    Intent intentLog =new Intent(SplashActivity.this, LoginActivity.class);
                    intentLog.putExtra("documentid", documentid);

                    startActivity(intentLog);
                    finish();
                }
            },time);
        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent gintent = getIntent();
                    if(gintent.getAction() == Intent.ACTION_VIEW){
                        //Log.d(TAG, "action_view, no session!");
                        documentid = gintent.getDataString().substring(82);

                        //Log.d(TAG, "gintent documentid: " + documentid);
                    }
                    Intent intentLog =new Intent(SplashActivity.this, LoginActivity.class);
                    intentLog.putExtra("documentid", documentid);
                    startActivity(intentLog);
                    finish();
                }
            },time);
        }
    }
}