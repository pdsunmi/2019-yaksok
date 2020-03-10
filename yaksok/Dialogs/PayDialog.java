package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yaksok.R;

public class PayDialog extends Dialog implements View.OnClickListener {

    private EditText editText;
    private Button btnComplete;
    private String price;
    private Context context;
    private PayDialogListener payDialogListener;

    public PayDialog(Context context, String price) {
        super(context);
        this.context = context;
        this.price = price;
    }

    public interface PayDialogListener {
        void onCompleteListener(String editValue);
    }

    public void setDialogListener(PayDialogListener payDialogListener) {
        this.payDialogListener = payDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_pay);
        editText = findViewById(R.id.edit_iprice);
        btnComplete = findViewById(R.id.btn_edit_iprice);
        if(price != "")
            editText.setText(price.substring(0, price.length()-1));
        else
            editText.setText("");
        btnComplete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_edit_iprice:
                payDialogListener.onCompleteListener(editText.getText().toString());
                dismiss();
                break;
        }
    }
}