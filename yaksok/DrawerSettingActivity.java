package com.example.yaksok;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;


public class DrawerSettingActivity extends Activity {

    private Button btnChange, btnSave;
    private EditText editName;
    private ImageView ivProfile;
    private String strName, strProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Dialog);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_setting);

        btnChange = findViewById(R.id.btn_change);
        btnSave = findViewById(R.id.btn_save);
        editName = findViewById(R.id.edit_name);
        ivProfile = findViewById(R.id.iv_profile);

        Intent intent = getIntent();
        strName = intent.getStringExtra("name");
        strProfile = intent.getStringExtra("profile");

        editName.setText(strName);
        // Image 처리
        if(strProfile == null) {
            Glide.with(this)
                    .load(R.drawable.ic_user)
                    .centerCrop()
                    .circleCrop()
                    .into(ivProfile);
        }
        else if(strProfile.substring(0,3).equals("htt")){
            Glide.with(this)
                    .load(strProfile)
                    .centerCrop()
                    .circleCrop()
                    .into(ivProfile);
        }
        else {
            Glide.with(getApplicationContext()).asBitmap()
                    .load(Uri.parse(strProfile))
                    .centerCrop()
                    .circleCrop()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            ivProfile.setImageBitmap(resource);
                        }
                    });
        }
        btnChange.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, 200);
            }
        });
        btnSave.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editName.getText()!= null) {
                    strName = editName.getText().toString();
                }
                Intent intent = new Intent();
                intent.putExtra("name", strName);
                intent.putExtra("profile", strProfile);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 200) {
            if (resultCode == RESULT_OK && data != null) {
                Uri dataUri = data.getData();
                try {
                    if(dataUri != null) {
                        strProfile = dataUri.toString();
                        Glide.with(getApplicationContext()).asBitmap()
                                .load(dataUri)
                                .centerCrop()
                                .circleCrop()
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                        ivProfile.setImageBitmap(resource);
                                    }
                                });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
