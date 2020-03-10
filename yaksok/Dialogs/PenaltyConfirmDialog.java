package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yaksok.R;

public class PenaltyConfirmDialog extends Dialog implements View.OnClickListener {

    private EditText editPenalty;
    private Button btnComplete;
    private String yaksokPenalty;
    private Context context;
    private PenaltyConfirmDialogListener penaltyConfirmDialogListener;

    public PenaltyConfirmDialog(Context context, String yaksokPenalty) {
        super(context);
        this.context = context;
        this.yaksokPenalty = yaksokPenalty;
    }

    public interface PenaltyConfirmDialogListener {
        void onCompleteListener(String editValue);
    }

    public void setDialogListener(PenaltyConfirmDialogListener penaltyConfirmDialogListener) {
        this.penaltyConfirmDialogListener = penaltyConfirmDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_penalty_c);
        editPenalty = findViewById(R.id.edit_penalty);
        btnComplete = findViewById(R.id.btn_edit_penalty);
        editPenalty.setText(yaksokPenalty);
        btnComplete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_edit_penalty:
                penaltyConfirmDialogListener.onCompleteListener(editPenalty.getText().toString());
                Log.d("edit Value : ", editPenalty.getText().toString());
                dismiss();
                break;
        }
}
}


