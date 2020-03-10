package com.example.yaksok;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.kakao.network.ApiErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String strNickname, strProfile, strCredit, strCreditid, strDocid;
    private String fbName, fbProfile;
    private TextView tvNickname, tvNickname2;
    private ImageView ivProfile, ivProfile2, ivProfileedge, ivProfileedge2;
    private Integer count, membercount;
    private Integer cnt = 0;
    private boolean isChanged;
    private String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private View drawerView;
    public static Context mContext;

    LinearLayout Llayout;
    ArrayList<Button> buttons;
    LinearLayout.LayoutParams parms;
    SwipeRefreshLayout swipeRefreshLayout;
    Boolean isfixed;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        Intent intent = getIntent();
        strNickname = intent.getStringExtra("name");
        strProfile = intent.getStringExtra("profile");
        strCreditid = intent.getStringExtra("Credit");
        strDocid = intent.getStringExtra("documentid");

        if (strDocid != null) {
            fbisfixed(strDocid);
        }
        tvNickname = findViewById(R.id.tvNickname);
        tvNickname2 = findViewById(R.id.tvNickname2);
        ivProfile = findViewById(R.id.ivProfile);
        ivProfile2 = findViewById(R.id.ivProfile2);
        ivProfileedge = findViewById(R.id.ivProfileedge);
        ivProfileedge2 = findViewById(R.id.ivProfile2edge);
        FloatingActionButton fab = findViewById(R.id.fab);
        Button btnSetting = findViewById(R.id.btnSetting);
        Button btnAlarm = findViewById(R.id.btnAlarm);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnSignout = findViewById(R.id.btnSignout);
        Button btnopen = findViewById(R.id.btn_open);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View) findViewById(R.id.drawer);
        Llayout = (LinearLayout) findViewById(R.id.LLayout);

        parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        parms.topMargin = 50;
        buttons = new ArrayList<>();
        count = 1;

        swipe();
        getsqldb();
        getCreditScore();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //NewActivity로 가는 인텐트를 생성
                Intent intent = new Intent(getApplicationContext(), NewActivity.class);
                intent.putExtra("name", strNickname); //유저 이름(String)
                intent.putExtra("id", strCreditid); //유저 이름(String)
                intent.putExtra("profile", strProfile);
                //액티비티 시작
                startActivity(intent);
            }
        });
        btnopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });
        drawerLayout.setDrawerListener((listener));
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        btnSetting.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DrawerSettingActivity.class);
                intent.putExtra("name", strNickname);
                intent.putExtra("profile", strProfile);
                startActivityForResult(intent, 300);
            }
        });
        btnAlarm.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DrawerAlarmActivity.class);
                startActivity(intent);
            }
        });
        btnLogout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "정상적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show(); //로그아웃 Toast 메세지

                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        });
        btnSignout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("탈퇴하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                                    @Override
                                    public void onFailure(ErrorResult errorResult) {
                                        int result = errorResult.getErrorCode();

                                        if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                                            Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "회원탈퇴에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onSessionClosed(ErrorResult errorResult) {
                                        Toast.makeText(getApplicationContext(), "로그인 세션이 닫혔습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onNotSignedUp() {
                                        Toast.makeText(getApplicationContext(), "가입되지 않은 계정입니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onSuccess(Long result) {
                                        Toast.makeText(getApplicationContext(), "회원탈퇴에 성공했습니다.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        //주기적으로 Fix된 약속들의 알람이 울려야 하는지 체크하고 울려야 할 때 알람을 발생시킴
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    //모든 약속 리스트를 가져옴
                    DBHelper helper = new DBHelper(this);
                    SQLiteDatabase sqldb = helper.getWritableDatabase();
                    Cursor yid_cursor = sqldb.rawQuery("select distinct _docid from IdTabel order by _num desc", null);
                    Integer yidsize = yid_cursor.getCount();
                    //Log.d("MainActivity: yidsize: ", yidsize.toString());
                    if (yidsize > 0) {
                        yid_cursor.moveToFirst();
                        String docid = yid_cursor.getString(yid_cursor.getColumnIndex("_docid"));
                        checkAndShowAlram(docid);
                        //Log.d("MainActivity: get sql: ", docid);
                        while (yid_cursor.moveToNext()) {
                            //Log.d("MainActivity:", "movetoNext ok");
                            docid = yid_cursor.getString(yid_cursor.getColumnIndex("_docid"));
                            //Log.d("MainActivity: get sql: ", docid);
                            checkAndShowAlram(docid);
                        }
                    }
                    yid_cursor.close();
                },
                0,
                20000,
                TimeUnit.MILLISECONDS);
    }

    private void checkAndShowAlram(String docid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String documentid = docid;
        DocumentReference docRef = db.collection("Yaksok").document(docid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //날짜가 fix 되었는지 확인
                        Log.d("MainActivity", "check " + document.get("Name"));
                        boolean isFixedTime = document.get("fixdatetime") != null;
                        if (isFixedTime) {
                            Timestamp timestamp = (Timestamp) document.get("fixdatetime");
                            //알람 리스트를 가져옴
                            List<String> list = (List<String>) document.get("alarm");
                            출처: https://jekalmin.tistory.com/entry/자바-18-날짜-정리 [jekalmin의 블로그]
                            for (String s : list) {
                                if (s != null) {
                                    //현재 시간과 알람이 울려야 할 시간이 일치하는지 확인
                                    if(isEqulsDate(timestamp.toDate(), Long.parseLong(s))) {
                                        showAlarm((String) document.get("Name"), s);
                                    }
                                    /*if (date.truncatedTo(ChronoUnit.MINUTES).equals(ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES))) {
                                        showAlarm((String) document.get("Name"), s);
                                    }*/
                                }
                            }
                        }
                    } else {
                        //Log.d(TAG, "No such document. delete sql record");
                        deletesqldb(documentid);
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private boolean isEqulsDate(Date date, long minute) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        calendar1.set(Calendar.SECOND,0);
        calendar1.set(Calendar.MILLISECOND,0);
        calendar1.add(Calendar.MINUTE, (int) -minute);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(new Date());
        calendar2.set(Calendar.SECOND,0);
        calendar2.set(Calendar.MILLISECOND,0);

        return calendar1.equals(calendar2);
    }

    //푸쉬 알람을 전시
    private void showAlarm(String yaksok, String minute) {
        //푸쉬 알람을 초기화
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.drawable.icon4cm);
        builder.setContentTitle("약속명: " + yaksok);
        builder.setContentText("약속 "+ (minute.equals("60") ? "1시간전" :  minute + "분전"));

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        notificationManager.notify(1, builder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 300) {
            if (resultCode == RESULT_OK && data != null) {
                strNickname = data.getStringExtra("name");
                strProfile = data.getStringExtra("profile");
                isChanged = true;

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("Credit").document(strCreditid);
                docRef.update("isChanged", true);
                docRef.update("name", strNickname);
                docRef.update("profile", strProfile);

                tvNickname.setText(strNickname);
                tvNickname2.setText(strNickname);

                Glide.with(getApplicationContext()).asBitmap()
                        .load(Uri.parse(strProfile))
                        .centerCrop()
                        .circleCrop()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                ivProfile.setImageBitmap(resource);
                                ivProfile2.setImageBitmap(resource);
                            }
                        });

                count = 1;
                DBHelper helper = new DBHelper(this);
                SQLiteDatabase sqldb = helper.getWritableDatabase();
                Cursor yid_cursor = sqldb.rawQuery("select distinct _docid from IdTabel order by _num desc", null);
                Integer yidsize = yid_cursor.getCount();
                if (yidsize > 0) {
                    yid_cursor.moveToFirst();
                    String docid = yid_cursor.getString(yid_cursor.getColumnIndex("_docid"));
                    modifyYaksokInfo(docid);
                    while (yid_cursor.moveToNext()) {
                        docid = yid_cursor.getString(yid_cursor.getColumnIndex("_docid"));
                        modifyYaksokInfo(docid);
                    }
                }
                yid_cursor.close();
            }
        }
    }
    private void modifyYaksokInfo(String docid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String documentid = docid;
        DocumentReference docRef = db.collection("Yaksok").document(docid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Integer membercount = Integer.parseInt(document.get("membercount").toString());
                        for(int i = 1; i <= membercount; i++)
                        {
                            HashMap map = (HashMap) document.get("member" + i);
                            if(strCreditid.equals(map.get("Kid")))
                            {
                                map.replace("Kprofile", strProfile);
                                map.replace("Kname", strNickname);
                                docRef.update("member"+i, map);
                            }
                        }
                    }
                }
            }
        });
    }
    private void setprofileimage() {
        if(isChanged) {
            strNickname = fbName;
            strProfile = fbProfile;
        }
        tvNickname.setText(strNickname);
        tvNickname2.setText(strNickname);

        // Image 처리
        if(strProfile == null) {
            Glide.with(this)
                    .load(R.drawable.ic_user6)
                    .centerCrop()
                    .circleCrop()
                    .into(ivProfile);
            Glide.with(this)
                    .load(R.drawable.ic_user6)
                    .centerCrop()
                    .circleCrop()
                    .into(ivProfile2);
        }
        else if(strProfile.substring(0,3).equals("htt")){
            Glide.with(this)
                    .load(strProfile)
                    .centerCrop()
                    .circleCrop()
                    .into(ivProfile);
            Glide.with(this)
                    .load(strProfile)
                    .centerCrop()
                    .circleCrop()
                    .into(ivProfile2);
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
                            ivProfile2.setImageBitmap(resource);
                        }
                    });
        }
    }
    private void fbisfixed(final String documentid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        isfixed = document.getBoolean("fixed");
                        Log.d(TAG, "firebase fixed: " + isfixed);
                        if(isfixed ){
                            Log.d(TAG, "get documentid from SplashActivity-LoginActivity, but already fixed");
                            Toast T1 = Toast.makeText(getApplicationContext(), "이미 완성된 약속입니다", Toast.LENGTH_LONG);
                            T1.show();
                        }
                        else if(!isfixed){
                            Log.d(TAG, "get documentid : " + strDocid);
                            if(strDocid != null){
                                Log.d(TAG, "2. get documentid from SplashActivity-LoginActivity, not fixed");
                                fbgetmembercount(strDocid);
                                getsqldb();
                            }
                        }
                    } else {
                        //Log.d(TAG, "No such document. delete sql record");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    private void swipe() {

        swipeRefreshLayout = findViewById(R.id.swipeview);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getsqldb();
                //Log.d(TAG, "refresh success");
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }
    public void getsqldb() {
        Llayout.removeAllViews();
        count = 1;
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase sqldb = helper.getWritableDatabase();
        Cursor yid_cursor = sqldb.rawQuery("select distinct _docid from IdTabel order by _num desc", null);
        Integer yidsize = yid_cursor.getCount();
        //Log.d("MainActivity: yidsize: ", yidsize.toString());
        if (yidsize > 0) {
            yid_cursor.moveToFirst();
            String docid = yid_cursor.getString(yid_cursor.getColumnIndex("_docid"));
            //Log.d("MainActivity: get sql: ", docid);
            getYaksokInfo(docid);
            while (yid_cursor.moveToNext()) {
                //Log.d("MainActivity:", "movetoNext ok");
                docid = yid_cursor.getString(yid_cursor.getColumnIndex("_docid"));
                //Log.d("MainActivity: get sql: ", docid);
                getYaksokInfo(docid);
            }
        }
        yid_cursor.close();
    }
    private void getYaksokInfo(String docid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String documentid = docid;
        DocumentReference docRef = db.collection("Yaksok").document(docid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String YaksokName = document.get("Name").toString();
                        //Log.d(TAG, "DocumentSnapshot data: " + YaksokName);
                        Button newbtn = new Button(MainActivity.this);
                        newbtn.setText(YaksokName);
                        newbtn.setTextColor(getResources().getColor(R.color.colorGray));
                        newbtn.setId((count++));
                        newbtn.setLayoutParams(parms);
                        if (count % 2 == 0) {
                            newbtn.setBackgroundColor(Color.parseColor("#FEEFEC"));
                        } else {
                            newbtn.setBackgroundColor(Color.parseColor("#FFFCE4"));
                        }
                        newbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), YaksokActivity.class);
                                intent.putExtra("docid", documentid);
                                intent.putExtra("kakaoname", strNickname);
                                intent.putExtra("id", strCreditid);
                                intent.putExtra("profile", strProfile);
                                intent.putExtra("credit",strCredit);
                                startActivity(intent);
                            }
                        });

                        buttons.add(newbtn);
                        Llayout.addView(newbtn);

                    } else {
                        //Log.d(TAG, "No such document. delete sql record");
                        deletesqldb(documentid);
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    private void deletesqldb(String documentid) {
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase sqldb = helper.getWritableDatabase();
        sqldb.execSQL("DELETE FROM IdTabel WHERE _docid = '" + documentid + "';");
        //Log.d(TAG, "sql db delete record where yaksokid: "+ documentid);
    }
    private void getCreditScore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Credit").document(strCreditid);
        docRef.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    isChanged = snapshot.getBoolean("isChanged");
                    fbName = snapshot.getString("name");
                    fbProfile = snapshot.getString("profile");
                    strCredit = snapshot.get("score").toString();

                    Log.d(TAG, "Snapshot data: " + isChanged);

                    setprofileimage();
                    Integer credit = Integer.parseInt(strCredit);
                    if(credit >= 80)
                        setbackground(1);
                    else if(credit <= 50)
                        setbackground(0);
                    TextView tvCredit = findViewById(R.id.tvcreditvalue);
                    TextView tvCredit2 = findViewById(R.id.tvcreditvalue2);
                    tvCredit.setText(strCredit);
                    tvCredit2.setText(strCredit);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
    private void setbackground(int i) {
        if(i == 1) {
            ivProfileedge.setBackground(ContextCompat.getDrawable(this, R.drawable.credithigh));
            ivProfileedge2.setBackground(ContextCompat.getDrawable(this, R.drawable.credithigh));
        }
        else if(i == 0) {
            ivProfileedge.setBackground(ContextCompat.getDrawable(this, R.drawable.creditlow));
            ivProfileedge2.setBackground(ContextCompat.getDrawable(this, R.drawable.creditlow));
        }
    }
    private void fbgetmembercount(String documentid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(strDocid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String getmc = document.get("membercount").toString();
                        Log.d(TAG, "firebase membercount: " + getmc);
                        membercount = Integer.parseInt(getmc);
                        Log.d(TAG, "membercount to parseInt: " + membercount);
                        membercount++;
                        Log.d(TAG, "membercount++: " + membercount);
                        fbsetmyinfo();
                        getcount();
                    } else {
                        Log.d(TAG, "No such document. delete sql record");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    private void fbaddboard() {
        cnt++;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Board").document(strDocid)
                .update("m"+cnt, strNickname+"님이 약속에 참가했습니다");
        //Log.d(TAG, "m+cnt: "+ cnt);
        //Log.d(TAG, "strNickname: "+ strNickname);

        db.collection("Board").document(strDocid)
                .update("count", cnt);
        //Log.d(TAG, "cnt++: "+ cnt);
    }
    private void getcount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Board").document(strDocid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "getcount start");
                        String getc = document.get("count").toString();
                        cnt = Integer.parseInt(getc);
                        Log.d(TAG, "count: " + cnt);
                        fbaddboard();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    private void fbsetmyinfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Yaksok").document(strDocid)
                .update("membercount", membercount);

        Map<String, Object> MemberData = new HashMap<>();
        MemberData.put("Kname", strNickname);
        MemberData.put("Kid", strCreditid);
        MemberData.put("Kprofile", strProfile);
        db.collection("Yaksok").document(strDocid)
                .update("member"+membercount, MemberData);
    }
    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };
}
