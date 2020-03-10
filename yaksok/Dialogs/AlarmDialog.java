package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yaksok.R;

public class AlarmDialog extends Dialog implements View.OnClickListener {
    private String Yaksok_alarm;
    private Button btnNone, btn5Min, btn15Min, btn30Min, btn1Hour;
    private Context context;
    private AlarmDialogListener alarmDialogListener;

    public AlarmDialog(Context context) {
        super(context);
        this.context = context;
    }

    public interface AlarmDialogListener {
        void onCompleteListener(String toast, String ya);
    }

    public void setDialogListener(AlarmDialogListener alarmDialogListener) {
        this.alarmDialogListener = alarmDialogListener;
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
                alarmDialogListener.onCompleteListener("알람 없음", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn5Min:
                Yaksok_alarm = "5";
                alarmDialogListener.onCompleteListener("5분전 설정", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn15Min:
                Yaksok_alarm = "15";
                alarmDialogListener.onCompleteListener("15분전 설정", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn30Min:
                Yaksok_alarm = "30";
                alarmDialogListener.onCompleteListener("30분전 설정", Yaksok_alarm);
                dismiss();
                break;
            case R.id.btn1Hour:
                Yaksok_alarm = "1";
                alarmDialogListener.onCompleteListener("1시간전 설정", Yaksok_alarm);
                dismiss();
                break;
        }
    }
}
