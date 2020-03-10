package com.example.yaksok;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.yaksok.Dialogs.PayDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PayActivity extends AppCompatActivity {

    private String documentid;
    private Integer membercount;
    private EditText editPrice;
    private Button btnSave;
    private LinearLayout layoutPay, layoutPay2;
    private int profileID, idID, creditID, priceID, modifyID;
    private int max = 7;
    public RequestManager mGlideRequestManager;
    public InputMethodManager imm;
    ArrayList<Map> memberDataList;
    List<String> payList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        documentid = getIntent().getStringExtra("docid");
        editPrice = findViewById(R.id.editPrice);
        layoutPay = findViewById(R.id.layoutPay);
        layoutPay2 = findViewById(R.id.layoutPay2);
        btnSave = findViewById(R.id.btn_save);

        for(int i=1;i <= max;i++) {
            profileID = getResources().getIdentifier("profile" + i, "id", "com.example.yaksok");
            idID = getResources().getIdentifier("id" + i, "id", "com.example.yaksok");
            creditID = getResources().getIdentifier("credit" + i, "id", "com.example.yaksok");
            priceID = getResources().getIdentifier("price" + i, "id", "com.example.yaksok");
            modifyID = getResources().getIdentifier("modify" + i, "id", "com.example.yaksok");
        }
        profileID = profileID - max;
        idID = idID - max;
        creditID = creditID - max;
        priceID = priceID - max;
        modifyID = modifyID - max;

        memberDataList = new ArrayList<>();
        mGlideRequestManager = Glide.with(this);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        layoutPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editPrice.getWindowToken(), 0);
            }
        });
        layoutPay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editPrice.getWindowToken(), 0);
            }
        });
        getfbkakaoinfo();
    }

    private void getfbkakaoinfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        // Show ID, profile
                        membercount = Integer.parseInt(document.get("membercount").toString());
                        payList = (List<String>) document.get("payList");
                        for(int i = 1; i <= membercount; i++)
                        {
                            Map<String, Object> memberData = (Map<String, Object>) document.get("member"+i);
                            memberDataList.add(memberData);
                            // Image 처리
                            if(memberData.get("Kprofile") == null) {
                                mGlideRequestManager
                                        .load(R.drawable.ic_user)
                                        .centerCrop()
                                        .circleCrop()
                                        .into((ImageView)findViewById(profileID+i));
                            }
                            else if(memberData.get("Kprofile").toString().substring(0,3).equals("htt")){
                                mGlideRequestManager
                                        .load(memberData.get("Kprofile").toString())
                                        .centerCrop()
                                        .circleCrop()
                                        .into((ImageView)findViewById(profileID+i));
                            }
                            else {
                                ImageView iv = (ImageView)findViewById(profileID+i);
                                Glide.with(getApplicationContext()).asBitmap()
                                        .load(Uri.parse(memberData.get("Kprofile").toString()))
                                        .centerCrop()
                                        .circleCrop()
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                                iv.setImageBitmap(resource);
                                            }
                                        });
                            }
                            ((TextView)findViewById(priceID+i)).setText("");
                            if(payList.get(i-1) != null) {
                                ((TextView) findViewById(priceID + i)).setText(payList.get(i - 1) + "원");
                            }
                            ((ImageView)findViewById(profileID+i)).setVisibility(View.VISIBLE);
                            ((TextView)findViewById(idID+i)).setText(memberData.get("Kname").toString());
                            ((TextView)findViewById(idID+i)).setVisibility(View.VISIBLE);
                            ((Button) findViewById(modifyID + i)).setVisibility(View.VISIBLE);
                            getfbcreditinfo(i);
                        }
                        // if editPrice data exists
                        if(document.get("pay") != null) {
                            String saved = document.get("pay").toString();
                            editPrice.setText(saved);
                            int price = Integer.parseInt(saved);
                            for(int i = 1; i <= membercount; i++) {
                                if(payList.get(i-1)==null)
                                    ((TextView)findViewById(priceID+i)).setText(price/membercount+ "원");
                                else
                                    ((TextView)findViewById(priceID+i)).setText(payList.get(i-1)+ "원");
                                ((TextView)findViewById(priceID+i)).setVisibility(View.VISIBLE);
                            }
                        }
                        // modify onClick
                        for(int i = 1 ;i <= membercount ; i++) {
                            ((Button)findViewById(modifyID+i)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imm.hideSoftInputFromWindow(editPrice.getWindowToken(), 0);
                                    showModify(v.getId()-modifyID);
                                }
                            });
                        }
                        // save onClick
                        btnSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String pay = editPrice.getText().toString();
                                if(pay.equals(""))
                                    docRef.update("pay", null);
                                else
                                    docRef.update("pay", pay);
                                docRef.update("payList", payList);
                                finish();
                            }
                        });
                        // editPrice Changed
                        editPrice.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }
                            @Override
                            public void afterTextChanged(Editable s) {
                                String input =  s.toString();
                                if(!input.isEmpty() && input != null) {
                                    try {
                                        int price = Integer.parseInt(input);
                                        for (int i = 1; i <= membercount; i++) {
                                            ((TextView) findViewById(priceID + i)).setText(price / membercount + "원");
                                            ((TextView) findViewById(priceID + i)).setVisibility(View.VISIBLE);
                                            payList.set(i-1, null);
                                        }
                                    } catch(NumberFormatException e) {editPrice.setText(null);}
                                }
                                else {
                                    for(int i = 1; i <= membercount; i++) {
                                        ((TextView) findViewById(priceID+i)).setVisibility(View.INVISIBLE);
                                        payList.set(i-1, null);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    private void showModify(int i) {

        PayDialog payDialog = new PayDialog(this, ((TextView) findViewById(priceID + i)).getText().toString());
        payDialog.setDialogListener(new PayDialog.PayDialogListener() {
            @Override
            public void onCompleteListener(String editValue) {
                ((TextView) findViewById(priceID + i)).setText(editValue + "원");
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("Yaksok").document(documentid);
                payList.set(i - 1, editValue);
                docRef.update("payList", payList);
            }
        });
        payDialog.show();
    }
    private void getfbcreditinfo(int i) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String kakaoid = memberDataList.get(i - 1).get("Kid").toString();
        DocumentReference docRef = db.collection("Credit").document(kakaoid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ((TextView) findViewById(creditID + i)).setText("신용도 : " + document.get("score").toString());
                        ((TextView) findViewById(creditID + i)).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}


