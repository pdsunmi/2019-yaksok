package com.example.yaksok;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.yaksok.Dialogs.AlarmConfirmDialog;
import com.example.yaksok.Dialogs.AlarmDialog;
import com.example.yaksok.Dialogs.CalendarDialog;
import com.example.yaksok.Dialogs.DateConfirmDialog;
import com.example.yaksok.Dialogs.FixDateTimeDialog;
import com.example.yaksok.Dialogs.PenaltyConfirmDialog;
import com.example.yaksok.Dialogs.PenaltyDialog;
import com.example.yaksok.Dialogs.SelectTimeDialog;
import com.example.yaksok.Models.Promise;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class YaksokActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "YaksokActivity";
    String documentid;
    String kakaoname;
    String creatorid;
    String kakaoid;
    String kakaoprofile;
    Integer cnt = 0;
    Boolean isfixed;
    private Button yscPlace;
    private Button btn_ysMemo, btn_ysStatus, btn_ysPenalty, btn_ysMember;
    private Button btn_ysDate, btn_ysAlarm, btn_ysPay;
    private String memo;
    private String YaksokName, YaksokPlace, YaksokPlaceId, YaksokPenalty, YaksokAlarm, YaksokMember;
    private Double YaksokPlaceLat, YaksokPlaceLng;
    private Integer memberid, credit;
    private Long membercount, selectcount;
    private boolean isFixedDate;
    private boolean isFixedTime;

    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    private boolean arrived = false;
    private final String BROADCAST_MESSAGE = "com.example.yaksok.broadcast";
    private Calendar calendar;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private BroadcastReceiver br;
    private CalendarDialog calendarDialog;
    private SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd HH:mm:ss");
    private boolean isIDecidedDate = false;
    private DateConfirmDialog dateConfirmDialog;
    private SelectTimeDialog timeCofirmDialog;
    List<HashMap<String, Object>> dateObjectList;
    List<String> alarmList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yaksok);

        memo = "";
        Intent intent = getIntent();
        documentid = intent.getStringExtra("docid");
        kakaoname = intent.getStringExtra("kakaoname");
        kakaoid = intent.getStringExtra("id");
        kakaoprofile = intent.getStringExtra("profile");
        credit = Integer.parseInt(intent.getStringExtra("credit"));


        getfbysinfo();
        getcount();
        view_init();

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(BROADCAST_MESSAGE);

        br= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_MESSAGE)) {

                    settext("약속시간 입니다");
                    if(!arrived) {
                        settext(kakaoname + "님이 지각 하셨습니다");
                        givepenalty();
                    }
                    else {
                        giveplus();
                    }
                }
            }
        };

        registerReceiver(br, theFilter);

    }
    public void setAlarm(int yd,int yh,int ym){

        Intent intent = new Intent(BROADCAST_MESSAGE);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2019);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DATE, yd);
        calendar.set(Calendar.HOUR_OF_DAY, yh);
        calendar.set(Calendar.MINUTE, ym);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long aTime = System.currentTimeMillis();
        long bTime = calendar.getTimeInMillis();

        System.out.println("set Alarm");

        alarmManager.set(AlarmManager.RTC, bTime, pendingIntent);


        if(aTime>bTime){
            System.out.println("cancel Alarm");
            alarmManager.cancel(pendingIntent);
        }
    }
    private void givepenalty() {
        settext(kakaoname +"님에게 벌금 "+YaksokPenalty+"원이 부과됩니다");
        credit = credit - 5;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Credit").document(kakaoid);
        docRef.update("score", credit.toString());
        settext(kakaoname+"님의 신용도가 5점 차감되었습니다");

    }
    private void giveplus() {
        credit = credit + 5;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Credit").document(kakaoid);
        docRef.update("score", credit.toString());
        settext(kakaoname+"님의 신용도가 5점 추가되었습니다");

    }
    private void getcount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Board").document(documentid);
        docRef.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String getc = snapshot.get("count").toString();
                    cnt = Integer.parseInt(getc);
                    //Log.d(TAG, "count: " + cnt);
                    getfbtext();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }


    private void getfbtext() {
        if (cnt != 0) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("Board").document(documentid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String board = "";
                            for (int i = 1; i <= cnt; i++) {
                                String message = document.get("m" + i).toString();
                                if (i == cnt) {
                                    board += message;
                                } else {
                                    board += message + "\n";
                                }
                                //Log.d(TAG, "Snapshot data: " + message);
                            }
                            TextView tvboard = findViewById(R.id.board);
                            tvboard.setText(board);
                        } else {
                            Log.d(TAG, "No such document. delete sql record");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    private void getfbysinfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        YaksokName = document.get("Name").toString();
                        YaksokPlace = document.get("place").toString();
                        YaksokPlaceLat = Double.parseDouble(document.get("place_lat").toString());
                        YaksokPlaceLng = Double.parseDouble(document.get("place_lng").toString());
                        if (document.get("place_id") != null)
                            YaksokPlaceId = document.get("place_id").toString();
                        YaksokPenalty = document.get("penalty").toString();
                        creatorid = document.get("creatorid").toString();
                        if(kakaoid!=creatorid)
                            memberid = Integer.parseInt(document.get("membercount").toString());
                        else
                            memberid = 1;


                        TextView nameview = findViewById(R.id.ysname);
                        nameview.setText(YaksokName);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void view_init() {

        yscPlace = findViewById(R.id.btn_ysPlace);
        yscPlace.setOnClickListener(this);

        btn_ysMemo = findViewById(R.id.btn_ysMemo);
        btn_ysMemo.setOnClickListener(this);

        btn_ysStatus = findViewById(R.id.btn_ysStatus);
        btn_ysStatus.setOnClickListener(this);

        btn_ysPenalty = findViewById(R.id.btn_ysPenalty);
        btn_ysPenalty.setOnClickListener(this);

        btn_ysDate = findViewById(R.id.btn_ysDate);
        btn_ysDate.setOnClickListener(this);

        btn_ysMember = findViewById(R.id.btn_ysMember);
        btn_ysMember.setOnClickListener(this);

        btn_ysAlarm = findViewById(R.id.btn_ysAlarm);
        btn_ysAlarm.setOnClickListener(this);

        btn_ysPay = findViewById(R.id.btn_ysPay);
        btn_ysPay.setOnClickListener(this);

        FloatingActionButton fab_delete = findViewById(R.id.fab_delete);
        fab_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (kakaoid.equals(creatorid)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(YaksokActivity.this, R.style.AlertDialogStyle);
                    builder.setMessage("정말 약속을 삭제하시겠습니까?");
                    builder.setTitle("삭제")
                            .setCancelable(true)
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deletefbYaksok();
                                    deletefbYBoard();
                                    Toast.makeText(YaksokActivity.this, "약속을 삭제하였습니다", Toast.LENGTH_SHORT).show();
                                    ((MainActivity) MainActivity.mContext).getsqldb();
                                    finish();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.cancel();
                                    Toast.makeText(YaksokActivity.this, "취소하였습니다", Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.setTitle("삭제 알림창");
                    alert.show();
                } else {
                    Log.d(TAG, "creatorid: " + creatorid);
                    Log.d(TAG, "kakaoid: " + kakaoid);

                    AlertDialog.Builder builder = new AlertDialog.Builder(YaksokActivity.this, R.style.AlertDialogStyle);
                    builder.setMessage("정말 약속에서 나가시겠습니까?");
                    builder.setTitle("지우기")
                            .setCancelable(true)
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getmycount();
                                    deletesql(documentid);
                                    settext(kakaoname + "님이 나가셨습니다");
                                    Toast.makeText(YaksokActivity.this, "약속을 지웠습니다", Toast.LENGTH_SHORT).show();
                                    ((MainActivity) MainActivity.mContext).getsqldb();
                                    finish();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.cancel();
                                    Toast.makeText(YaksokActivity.this, "취소하였습니다", Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.setTitle("나가기 알림창");
                    alert.show();
                }
            }
        });
    }

    private void getmycount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String getmc = document.get("membercount").toString();
                        Integer membercount = Integer.parseInt(getmc);
                        Log.d(TAG, "firebase membercount: " + membercount);
                        Log.d(TAG, "kakaoid: " + kakaoid);

                        for (int i = 1; i <= membercount; i++) {
                            HashMap map = (HashMap) document.get("member" + i);
                            String memberid = map.get("Kid").toString();
                            Log.d(TAG, i + "member's id: " + memberid);
                            if (kakaoid.equals(memberid)) {
                                Log.d(TAG, "found" + i);
                                deleteYaksokmyInfo(i);
                            }
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void deletesql(String documentid) {
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase sqldb = helper.getWritableDatabase();
        sqldb.execSQL("DELETE FROM IdTabel WHERE _docid = '" + documentid + "';");
        //Log.d(TAG, "sql db delete record where yaksokid: "+ documentid);
    }

    private void deleteYaksokmyInfo(int mycount) {
        Log.d(TAG, "deleteYaksokmyInfo start" + mycount);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);

        // Remove the 'capital' field from the document
        Map<String, Object> updates = new HashMap<>();
        updates.put("Kid", "");
        updates.put("Kname", "");
        updates.put("Kprofile", "");
        docRef.update("member" + mycount, updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "deleteYaksokmyInfo member" + mycount + "success.");
            }
        });
    }

    private void deletefbYBoard() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Board").document(documentid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Board's document deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting Board's document", e);
                    }
                });
    }

    private void deletefbYaksok() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Yaksok").document(documentid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Yaksok's document deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting Yaksok's document", e);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ysPlace:
                Intent intent = new Intent(getApplicationContext(), PlaceConfirmActivity.class);
                intent.putExtra("place", YaksokPlace);
                intent.putExtra("place_id", YaksokPlaceId);
                startActivityForResult(intent, 100);
                break;
            case R.id.btn_ysMemo:
                showMemo();
                break;
            case R.id.btn_ysStatus:
                showStatus();
                break;
            case R.id.btn_ysPenalty:
                showPenalty();
                break;
            case R.id.btn_ysMember:
                showMember();
                break;
            case R.id.btn_ysDate:
                processSelectDateButton();
                break;
            case R.id.btn_ysAlarm:
                showAlarmSetting();
                break;
            case R.id.btn_ysPay:
                Intent intent2 = new Intent(getApplicationContext(), PayActivity.class);
                intent2.putExtra("docid", documentid);
                startActivity(intent2);
                break;
        }
    }


    //캘린더 창에서 날짜 확정 버튼을 눌렀을 때 처리
    private void saveDate() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //현재 db의 date 정보를 가져옴
                        dateObjectList = (List<HashMap<String, Object>>) document.get("date");
                        //캘린더에서 선택된 date들을 가져와서 순회
                        for (Promise promise : calendarDialog.getmPromiseList()) {
                            //선택된 date가 db의 date중에 있는지 탐색
                            Optional<HashMap<String, Object>> object =
                                    dateObjectList.stream().filter(e -> ((Timestamp) e.get("startTime")).toDate().getTime() == (promise.getDate())).findAny();
                            if (object.isPresent()) {
                                //있을 경우 db에 있는 date 정보의 selectCount를 1증가
                                dateObjectList.get(dateObjectList.indexOf(object.get())).put("selectCount", (Long) object.get().get("selectCount") + 1);
                            } else {
                                //없을 경우 date 정보를 추가
                                HashMap hashMap = new HashMap();
                                hashMap.put("startTime", new Timestamp(new Date()));
                                hashMap.put("selectCount", 1);
                                dateObjectList.add(hashMap);
                            }
                        }

                        //date 정보를 DB에 업데이트
                        db.collection("Yaksok").document(documentid)
                                .update("date", dateObjectList);
                        //selectcount를 증가시켜 DB에 업데이트
                        db.collection("Yaksok").document(documentid)
                                .update("selectcount", ((Long) document.get("selectcount")) + 1);
                        settext(kakaoname + "님이 날짜를 선택하셨습니다");
                        checkallsave();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void checkallsave() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //membercount, selectcount를 가져옴
                        membercount = (Long) document.get("membercount");
                        selectcount = (Long) document.get("selectcount");
                        if (membercount == selectcount) {
                            settext("모든 인원이 날짜를 선택하였습니다");
                        }

                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            }
        });
    }

    //날짜 선택 버튼을 눌렀을 때 동작
    private void processSelectDateButton() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //membercount, selectcount를 가져옴
                        membercount = (Long) document.get("membercount");
                        selectcount = (Long) document.get("selectcount");
                        Log.d(TAG, "membercount: " + membercount);
                        Log.d(TAG, "selectcount: " + selectcount);
                        //날짜가 확정이 되었는지
                        isFixedDate = document.get("fixdate") != null;
                        //시간이 확정이 되었는지
                        //시간이 확정이 되었는지
                        isFixedTime = document.get("fixdatetime") != null;
                        Log.d(TAG, "fixdate: " + isFixedDate);


                        if (isFixedTime) {
                            //약속 날짜 시간이 모두 선택이 되었으면
                            showFixDialog((Timestamp) document.get("fixdatetime"));
                            String s = document.get("fixdatetime").toString();
                            s = s.substring(18,28);
                            long timestamp = Long.parseLong(s) * 1000L;
                            Date fixedDate = (new Date(timestamp));

                            String yaksok_time = format.format(fixedDate);
                            int yd = Integer.parseInt(yaksok_time.substring(7,8));
                            int yh = Integer.parseInt(yaksok_time.substring(9,11));
                            int ym = Integer.parseInt(yaksok_time.substring(12,14));
                            setAlarm(yd,yh,ym);

                        } else if (!isFixedTime && isFixedDate) {
                            //날짜만 선택이 되었으면
                            showTimeConfirmDialog();
                            settext("약속 날짜와 시간이 확정되었습니다");
                        } else if (membercount == selectcount) {
                            //멤버들이 모두 날자를 선택한 경우
                            showDateConfirmDialog();
                            settext("약속 날짜가 확정되었습니다");
                        } else if (isIDecidedDate) {
                            //캘린더 창에서 이미 날짜를 결정한 경우 이미 결정하였다고 출력
                            Toast.makeText(getApplicationContext(), "이미 약속 날짜를 결정하셨습니다.", Toast.LENGTH_LONG).show();
                        } else {
                            //캘린더 전시
                            showCalendar();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void showFixDialog(Timestamp timestamp) {
        FixDateTimeDialog fixDateTimeDialog = new FixDateTimeDialog(this, timestamp);
        fixDateTimeDialog.show();
    }

    //날짜 확정 창 전시
    private void showDateConfirmDialog() {
        dateConfirmDialog = new DateConfirmDialog(this, documentid);
        dateConfirmDialog.show();
    }

    private void showTimeConfirmDialog() {
        timeCofirmDialog = new SelectTimeDialog(this, documentid);
        timeCofirmDialog.show();
    }

    //캘린더 전시
    private void showCalendar() {
        calendarDialog = new CalendarDialog(this, true, creatorid, kakaoid);
        calendarDialog.setConfirmListener(new CalendarDialog.ConfirmListener() {
            @Override
            public void onConfirm() {
                //날짜 확정 버튼이 눌렸을 경우 데이터를 저장하고 날짜 결정 여부를 true로 변경
                saveDate();
                isIDecidedDate = true;
            }
        });
        calendarDialog.setCanceledOnTouchOutside(false);
        calendarDialog.show();
    }

    private void showMemo() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_memo, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        EditText etMemo = popupView.findViewById(R.id.etMemo);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get("memo") != null)
                            etMemo.setText(document.get("memo").toString());
                    }
                }
            }
        });

        Button btnSave = popupView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                memo = etMemo.getText().toString();
                docRef.update("memo", memo);
                Toast.makeText(getApplicationContext(), "저장 완료", Toast.LENGTH_SHORT).show();
                settext("메모가 수정되었습니다");
                popupWindow.dismiss();
            }
        });

        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
    }

    private void showStatus() {

        View popupView = getLayoutInflater().inflate(R.layout.popup_my_status, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        Button btnWakeup = popupView.findViewById(R.id.btnWakeup);
        btnWakeup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFixedTime) {
                    settext(kakaoname + "님이 기상하셨습니다");
                    Toast.makeText(getApplicationContext(), "기상", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }
            }
        });

        Button btnGoing = popupView.findViewById(R.id.btnGoing);
        btnGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFixedTime) {
                    settext(kakaoname + "님이 출발하셨습니다");
                    Toast.makeText(getApplicationContext(), "출발", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }
            }
        });

        Button btnArrive = popupView.findViewById(R.id.btnArrive);
        btnArrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFixedTime) {
                    if(checkLocation()) {
                        System.out.println("장소 맞음");
                        checkTime();
                        popupWindow.dismiss();
                    }
                }
            }
        });
    }
    private void checkTime() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get("fixdate")!= null)
                        {
                            String s = document.get("fixdatetime").toString();
                            s = s.substring(18,28);
                            long timestamp = Long.parseLong(s) * 1000L;
                            Date fixedDate = (new Date(timestamp));

                            String yaksok_time = format.format(fixedDate);
                            String current_time = format.format(System.currentTimeMillis());

                            String yd = yaksok_time.substring(0, 8);
                            String cd = current_time.substring(0, 8);
                            int yh = Integer.parseInt(yaksok_time.substring(9,11));
                            int ch = Integer.parseInt(current_time.substring(9,11));
                            int ym = Integer.parseInt(yaksok_time.substring(12,14));
                            int cm = Integer.parseInt(current_time.substring(12,14));
                            int ys = Integer.parseInt(yaksok_time.substring(15,17));
                            int cs = Integer.parseInt(current_time.substring(15,17));


                            System.out.println("확정 시간 :" + yaksok_time);
                            System.out.println("현재 시간 :" + current_time);
                            if(yd.equals(cd)) {
                                if(yh > ch) {
                                    settext(kakaoname + "님이 도착하셨습니다");
                                    arrived = true;
                                    Toast.makeText(getApplicationContext(), "도착", Toast.LENGTH_SHORT).show();
                                }
                                else if(yh == ch) {
                                    if(ym > cm) {
                                        settext(kakaoname + "님이 도착하셨습니다");
                                        arrived = true;
                                        Toast.makeText(getApplicationContext(), "도착", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(ym == cm) {
                                        if(ys >= cs) {
                                            settext(kakaoname + "님이 도착하셨습니다");
                                            arrived = true;
                                            Toast.makeText(getApplicationContext(), "도착", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        });
    }
    private boolean checkLocation() {

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location userLocation = getMyLocation();
        if( userLocation != null ) {
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            System.out.println("현재 내 위치 : "+latitude+","+longitude);
            System.out.println("장소    위치 : "+YaksokPlaceLat+","+YaksokPlaceLng);

            final int earth = 6371000;
            int diff = 100000;
            double ddd = Math.cos(0);
            double ddf = Math.cos(Math.toRadians(latitude));

            double diffLatitude =(diff*360.0) / (2*Math.PI*earth);
            double diffLongitude = (diff*360.0) / (2*Math.PI*earth*Math.cos(Math.toRadians(YaksokPlaceLat)));
            System.out.println(YaksokPlaceLat-diffLatitude+" ~"+(YaksokPlaceLat+diffLatitude));
            System.out.println(YaksokPlaceLng-diffLongitude+" ~"+(YaksokPlaceLng+diffLongitude));
            if(latitude > YaksokPlaceLat-diffLatitude && latitude < YaksokPlaceLat+diffLatitude)
            {
                //if(longitude > YaksokPlaceLng-diffLatitude && YaksokPlaceLng > longitude +diffLatitude)
                //{
                  //  System.out.println("longitude");
                    return true;
                //}
            }
        }
        return false;
    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Permission Request to user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    this.REQUEST_CODE_LOCATION);
            getMyLocation();
        }
        else {
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }
        }
        return currentLocation;
    }

    private void settext(String message) {
        getcount();
        cnt++;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Board").document(documentid)
                .update("m" + cnt, message);

        db.collection("Board").document(documentid)
                .update("count", cnt);
    }

    private void showPenalty() {

        PenaltyConfirmDialog penaltyConfirmDialog = new PenaltyConfirmDialog(this, YaksokPenalty);
        penaltyConfirmDialog.setDialogListener(new PenaltyConfirmDialog.PenaltyConfirmDialogListener() {
            @Override
            public void onCompleteListener(String editValue) {

                Toast.makeText(YaksokActivity.this, "벌금 : " + editValue, Toast.LENGTH_LONG).show();
                Log.d("Yaksok Activity editValue:", "" + editValue);

                YaksokPenalty = editValue;
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Yaksok").document(documentid)
                        .update("penalty", YaksokPenalty);
                settext("벌금이 " + YaksokPenalty+"원으로 변경되었습니다");
            }
        });
        penaltyConfirmDialog.show();
    }

    private void showMember() {

        View popupView = getLayoutInflater().inflate(R.layout.popup_member, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        getfixed(popupView);
        getfbmember(popupView);
        Button btnWakeup = popupView.findViewById(R.id.btnfix);
        btnWakeup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isfixed) {
                    setbffix();
                    settext("약속 멤버가 확정되었습니다");
                    Toast.makeText(getApplicationContext(), "멤버가 확정되었습니다", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "이미 확정된 약속입니다", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }
            }
        });
    }

    private void getfixed(View popupView) {
        final View view = popupView;
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
                        if (!isfixed) {
                            TextView tvfix = view.findViewById(R.id.isfix);
                            tvfix.setText("현재까지 참여한 멤버");
                        } else {
                            TextView tvfix = view.findViewById(R.id.isfix);
                            tvfix.setText("확정된 약속 멤버");
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

    private void setbffix() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Yaksok").document(documentid)
                .update("fixed", true);
    }

    private void getfbmember(View popupView) {
        final View view = popupView;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String getmc = document.get("membercount").toString();
                        Integer membercount = Integer.parseInt(getmc);
                        //Log.d(TAG, "firebase membercount: " + membercount);
                        String membernames = "";


                        for (int i = 1; i <= membercount; i++) {
                            if (i == membercount) {
                                HashMap map = (HashMap) document.get("member" + i);
                                if (map.get("Kname") == "") {
                                    continue;
                                } else {
                                    membernames += map.get("Kname").toString();
                                }
                            } else {
                                HashMap map = (HashMap) document.get("member" + i);
                                if (map.get("Kname") == "") {
                                    continue;
                                } else {
                                    membernames += map.get("Kname").toString() + "\n";
                                }
                                //membernames += document.get("member" + i).toString() + "\n";
                                //Log.d(TAG, "membernames: " + membernames);
                            }
                        }
                        YaksokMember = membernames;
                        Log.d(TAG, "YaksokMember: " + YaksokMember);
                        TextView pn = view.findViewById(R.id.writehere);
                        pn.setText(YaksokMember);
                        Log.d(TAG, "setText: " + YaksokMember);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == 100) {
            String place_name = data.getStringExtra("place");
            String place_id = data.getStringExtra("place_id");
            Double place_lat = data.getDoubleExtra("place_lat", 0);
            Double place_lng = data.getDoubleExtra("place_lng", 0);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("Yaksok").document(documentid);
            docRef.update("place", place_name);
            docRef.update("place_id", place_id);
            docRef.update("place_lat", place_lat);
            docRef.update("place_lng", place_lng);
            YaksokPlace = place_name;
            YaksokPlaceId = place_id;
            YaksokPlaceLat = place_lat;
            YaksokPlaceLng = place_lng;
            settext("약속 장소가 " + YaksokPlace + "으로 변경되었습니다");
            Toast.makeText(this.getApplicationContext(), "장소 수정 : " + YaksokPlace, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlarmSetting() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        alarmList = (List<String>) document.get("alarm");

                        // 알람 설정했을때
                        if (alarmList.get(memberid-1) != null) {
                            YaksokAlarm = alarmList.get(memberid-1);
                        } else {
                            YaksokAlarm = null;
                        }

                        AlarmConfirmDialog alarmConfirmDialog = new AlarmConfirmDialog(YaksokActivity.this, YaksokAlarm);
                        alarmConfirmDialog.setDialogListener(new AlarmConfirmDialog.AlarmConfirmDialogListener() {
                            @Override
                            public void onCompleteListener(String toast, String ya) {
                                YaksokAlarm = ya;
                                Toast.makeText(YaksokActivity.this, toast, Toast.LENGTH_SHORT).show();
                                alarmList.set((memberid-1), YaksokAlarm);
                                docRef.update("alarm", alarmList);
                            }
                        });
                        alarmConfirmDialog.show();

                       // View popupView = getLayoutInflater().inflate(R.layout.popup_alarm_setting, null);
                        //final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        //popupWindow.setFocusable(true);
                        //popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
/*
                        Button btnNone = popupView.findViewById(R.id.btnNone);
                        Button btn5Min = popupView.findViewById(R.id.btn5Min);
                        Button btn15Min = popupView.findViewById(R.id.btn15Min);
                        Button btn30Min = popupView.findViewById(R.id.btn30Min);
                        Button btn1Hour = popupView.findViewById(R.id.btn1Hour);

                        if(YaksokAlarm!=null) {
                            switch (YaksokAlarm) {
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
                        btnNone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                YaksokAlarm = "0";
                                Toast.makeText(getApplicationContext(), "알람 없음", Toast.LENGTH_SHORT).show();
                                alarmList.set((memberid-1), YaksokAlarm);
                                docRef.update("alarm", alarmList);
                                popupWindow.dismiss();
                            }
                        });
                        btn5Min.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                YaksokAlarm = "5";
                                Toast.makeText(getApplicationContext(), "5분전 설정", Toast.LENGTH_SHORT).show();
                                alarmList.set((memberid-1), YaksokAlarm);
                                docRef.update("alarm", alarmList);
                                popupWindow.dismiss();
                            }
                        });
                        btn15Min.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                YaksokAlarm = "15";
                                Toast.makeText(getApplicationContext(), "15분전 설정", Toast.LENGTH_SHORT).show();
                                alarmList.set((memberid-1), YaksokAlarm);
                                docRef.update("alarm", alarmList);
                                popupWindow.dismiss();
                            }
                        });
                        btn30Min.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                YaksokAlarm = "30";
                                Toast.makeText(getApplicationContext(), "30분전 설정", Toast.LENGTH_SHORT).show();
                                alarmList.set((memberid-1), YaksokAlarm);
                                docRef.update("alarm", alarmList);
                                popupWindow.dismiss();
                            }
                        });
                        btn1Hour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                YaksokAlarm = "1";
                                Toast.makeText(getApplicationContext(), "1시간전 설정", Toast.LENGTH_SHORT).show();
                                alarmList.set((memberid-1), YaksokAlarm);
                                docRef.update("alarm", alarmList);
                                popupWindow.dismiss();
                            }
                        });*/
                    }
                }
            }
        });
    }
}



