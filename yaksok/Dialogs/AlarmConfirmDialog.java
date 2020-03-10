package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.yaksok.R;

public class AlarmConfirmDialog extends Dialog implements View.OnClickListener {
    private String Yaksok_alarm;
    private Button btnNone, btn5Min, btn15Min, btn30Min, btn1Hour;
    private Context context;
    private AlarmConfirmDialogListener alarmConfirmDialogListener;

    public AlarmConfirmDialog(Context context, String yaksok_alarm) {
        super(context);
        this.context = context;
        Yaksok_alarm = yaksok_alarm;
    }

    public interface AlarmConfirmDialogListener {
        void onCompleteListener(String toast, String ya);
    }

    public void setDialogListener(AlarmConfirmDialogListener alarmConfirmDialogListener) {
        this.alarmConfirmDialogListener = alarmConfirmDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_alarm_setting);

        btnNone = findViewById(R.id.btnNone);
        btn5Min = findViewById(R.id.btn5Min);
        btn15Min = findViewById(R.id.btn15Min);
        btn30Min = findViewById(R.id.btn30Min);
        btn1Hour = findViewById(R.id.btn1Hour);

        if(Yaksok_alarm!=null) {
            switch (Yaksok_alarm) {
                case "0":
                    btnNone.setBackgroundResource(R.drawable.buttonbackgroundwhite);
                    break;
                case "5":
                    btn5Min.setBackgroundResource(R.drawable.buttonbackgroundwhite);
                    break;
                case "15":
                    btn15Min.setBackgroundResource(R.drawable.buttonbackgroundwhite);
                    break;
                case "30":
                    btn30Min.setBackgroundResource(R.drawable.buttonbackgroundwhite);
                    break;
                case "1":
                    btn1Hour.setBackgroundResource(R.drawable.buttonbackgroundwhite);
                    break;
            }
        }
        btnNone.setOnClickListener(this);
        btn5Min.setOnClickListener(this);
        btn15Min.setOnClickListener(this);
        btn30Min.setOnClickListener(this);
        btn1Hour.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnNone:
                Yaksok_alarm = "0";
                System.out.println(Yaksok_alarm);
                alarmConfirmDialogListener.onCompleteListener("알람 없음", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn5Min:
                Yaksok_alarm = "5";
                alarmConfirmDialogListener.onCompleteListener("5분전 설정", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn15Min:
                Yaksok_alarm = "15";
                alarmConfirmDialogListener.onCompleteListener("15분전 설정", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn30Min:
                Yaksok_alarm = "30";
                alarmConfirmDialogListener.onCompleteListener("30분전 설정", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn1Hour:
                Yaksok_alarm = "1";
                alarmConfirmDialogListener.onCompleteListener("1시간전 설정", Yaksok_alarm);
                dismiss();
                break;
        }
    }
}
