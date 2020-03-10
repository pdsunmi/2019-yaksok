package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yaksok.R;

public class PenaltyDialog extends Dialog implements View.OnClickListener {
    private EditText editPenalty;
    private Button btnComplete;
    private Context context;
    private PenaltyDialogListener penaltyDialogListener;

    public PenaltyDialog(Context context) {
        super(context);
        this.context = context;
    }

    public interface PenaltyDialogListener {
        void onCompleteListener(String editValue);
    }

    public void setDialogListener(PenaltyDialogListener penaltyDialogListener) {
        this.penaltyDialogListener = penaltyDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_penalty);

        editPenalty = findViewById(R.id.edit_penalty);
        btnComplete = findViewById(R.id.btn_select_penalty);

        btnComplete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_select_penalty:
                penaltyDialogListener.onCompleteListener(editPenalty.getText().toString());
                Log.d("edit Value : ", editPenalty.getText().toString());
                dismiss();
                break;
        }
    }
}
