package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.yaksok.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class FixDateTimeDialog extends Dialog {
    String TAG = "FixDateTimeDialog";
    private Context context;
    private TextView dateTextView;
    private TextView timeTextView;
    private String date;
    private String time;
    private Button closeButton;
    public FixDateTimeDialog(@NonNull Context context, Timestamp time) {
        super(context);
        this.context = context;
        this.date = loadDate(time.toDate().getTime());
        this.time = loadTime(time.toDate().getTime());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_fix_date_time);
        dateTextView = findViewById(R.id.tv_date);
        timeTextView = findViewById(R.id.tv_time);
        closeButton = findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> {
            this.dismiss();
        });
        dateTextView.setText(date);
        timeTextView.setText(time);
    }

    /**
     * dateTime을 받아서 'YYYY년 MM월 DD일' 형태의 string으로 반환
     * @param dateTime
     * @return dateString
     */
    private String loadDate(Long dateTime) {
        Calendar cal = Calendar.getInstance();
        Date d = new Date(dateTime);
        cal.setTime(d);
        String ret = (cal.get(Calendar.YEAR)) + "년 " + (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DAY_OF_MONTH) + "일";
        return ret;
    }

    /**
     * dateTime을 받아서 'YYYY년 MM월 DD일' 형태의 string으로 반환
     * @param dateTime
     * @return dateString
     */
    private String loadTime(Long dateTime) {
        Calendar cal = Calendar.getInstance();
        Date d = new Date(dateTime);
        cal.setTime(d);
        String ret = (cal.get(Calendar.HOUR_OF_DAY)) + "시 " + (cal.get(Calendar.MINUTE)) + "분 ";
        return ret;
    }
}
