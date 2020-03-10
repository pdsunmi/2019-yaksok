package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.example.yaksok.R;

public class SelectTimeRangeDialog extends Dialog implements View.OnClickListener{
    private Context context;
    private TimePicker timePickerStart, timePickerEnd;
    private Button btnComplete;
    private SelectTimeDialogListener selectTimeDialogListener;
    public SelectTimeRangeDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_select_time_range);
        timePickerStart = findViewById(R.id.timePicker_start);
        timePickerEnd = findViewById(R.id.timePicker_end);
        btnComplete = findViewById(R.id.btn_select_time);

        btnComplete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_select_time:
                selectTimeDialogListener.onCompleteListener(timePickerStart.getHour(), timePickerStart.getMinute(), timePickerEnd.getHour(), timePickerEnd.getMinute());
                dismiss();
                break;
        }
    }

    public void setSelectTimeDialogListener(SelectTimeDialogListener selectTimeDialogListener) {
        this.selectTimeDialogListener = selectTimeDialogListener;
    }
    interface SelectTimeDialogListener {
        void onCompleteListener(int startHour, int startMin, int endHour, int endMin);
    }
}
