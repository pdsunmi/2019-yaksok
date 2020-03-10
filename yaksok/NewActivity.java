package com.example.yaksok;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yaksok.Dialogs.AlarmDialog;
import com.example.yaksok.Dialogs.CalendarDialog;
import com.example.yaksok.Dialogs.PenaltyDialog;
import com.example.yaksok.Models.Promise;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText ysName;
    private Button ysDate, ysPlace, ysPenalty, ysAlarm;
    private FloatingActionButton btn_final;
    String kako_Nickname, Yaksok_name, kako_id, kako_profile, Yaksok_place, Yaksok_place_id;
    String Yaksok_penalty = "0";
    String Yaksok_alarm ="0";
    Double Yaksok_place_lat, Yaksok_place_lng;

    String docid;
    private MaterialCalendarView materialCalendarView;
    Boolean isfinish = false;
    String TAG = "NewActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CalendarDialog calendarDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Intent intent = getIntent();
        kako_Nickname = intent.getStringExtra("name");
        kako_id = intent.getStringExtra("id");
        kako_profile = intent.getStringExtra("profile");

        view_init();

        btn_final.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDB_Yaksok(false);
                finish();
            }
        });

    }

    private void view_init() {
        //뷰 렌더링
        ysName = findViewById(R.id.ysName);
        ysDate = findViewById(R.id.ysDate);
        ysPlace = findViewById(R.id.ysPlace);
        ysPenalty = findViewById(R.id.ysPenalty);
        ysAlarm = findViewById(R.id.ysAlarm);
        btn_final = findViewById(R.id.btn_Final);

        Button btnkl = findViewById(R.id.ysMember);
        btnkl.setOnClickListener(this);
        ysDate.setOnClickListener(this);
        ysPlace.setOnClickListener(this);
        ysPenalty.setOnClickListener(this);
        ysAlarm.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ysMember:
                setDB_Yaksok(true);
                break;

            case R.id.ysDate:
                //날짜 선택을 누를시에 캘린더 팝업
                calendarDialog = new CalendarDialog(this, false,"1","0");
                calendarDialog.setCanceledOnTouchOutside(false);
                calendarDialog.show();
                break;

            case R.id.ysPenalty:
                //벌금 선택을 누를시에 벌금 팝업
                PenaltyDialog penaltyDialog = new PenaltyDialog(this);
                // 팝업창에서 벌금을 입력하고 확인을 눌렀을 때 NewActivity에서 값을 받음
                penaltyDialog.setDialogListener(new PenaltyDialog.PenaltyDialogListener() {
                    @Override
                    public void onCompleteListener(String editValue) {

                        Toast.makeText(NewActivity.this, "벌금 : " + editValue, Toast.LENGTH_SHORT).show();
                        Log.d("new Activity editValue:", "" + editValue);
                        Yaksok_penalty = editValue;
                    }
                });
                penaltyDialog.show();
                break;

            case R.id.ysPlace:
                Intent intent = new Intent(getApplicationContext(), PlaceActivity.class);
                startActivityForResult(intent, 100);
                break;

            case R.id.ysAlarm:
                AlarmDialog alarmDialog = new AlarmDialog(this);
                alarmDialog.setDialogListener(new AlarmDialog.AlarmDialogListener() {
                    @Override
                    public void onCompleteListener(String toast, String ya) {
                        System.out.println("oncomplete");
                        Yaksok_alarm = ya;
                        Toast.makeText(NewActivity.this, toast, Toast.LENGTH_SHORT).show();
                    }
                });
                alarmDialog.show();
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK)
            return;
        if(requestCode == 100) {
            Yaksok_place = data.getStringExtra("place");
            Yaksok_place_lat = data.getDoubleExtra("place_lat", 0);
            Yaksok_place_lng = data.getDoubleExtra("place_lng", 0);
            Yaksok_place_id = data.getStringExtra("place_id");
            if(Yaksok_place_id.equals("null"))
                Yaksok_place_id = null;
            Toast.makeText(this.getApplicationContext(),"장소 설정 : "+ Yaksok_place, Toast.LENGTH_SHORT).show();
        }
    }


    private void setDB_Yaksok(boolean from) {
        Yaksok_name = ((EditText)findViewById(R.id.ysName)).getText().toString();
        if(isfinish){
            //이미 약속 정보가 DB에 저장된 경우 스킵
            return;
        }
        else if(Yaksok_name.equals("") && from){
            //멤버선택 눌렀는데 약속 이름이 설정되지 않은 경우
            Log.d(TAG, "No name");
            Toast myToast = Toast.makeText(this.getApplicationContext(),"먼저 약속 이름을 입력해주세요", Toast.LENGTH_SHORT);
            myToast.show();
            return;
        }
        else if(Yaksok_name.equals("") && !from){
            //완료버튼 눌렀는데 약속 이름이 설정되지 않은 경우
            Log.d(TAG, "canceled");
            return;
        }
        else{
            final Boolean From = from;
            Map<String, Object> yaksok = new HashMap<>();
            yaksok.put("Name", Yaksok_name);
            if(Yaksok_place == null){
                Yaksok_place = "미정";
            }
            if(Yaksok_place_id != null)
                yaksok.put("place_id", Yaksok_place_id);
            yaksok.put("place", Yaksok_place);
            yaksok.put("place_lat", Yaksok_place_lat);
            yaksok.put("place_lng", Yaksok_place_lng);
            yaksok.put("penalty", Yaksok_penalty);
            yaksok.put("pay", null);
            yaksok.put("memo", null);

            List<String> alarmList = new ArrayList<>();
            alarmList.add(Yaksok_alarm);
            for(int i = 1;i <= 7;i++)
                alarmList.add(null);
            yaksok.put("alarm", alarmList);

            List<String> payList = new ArrayList<>();
            for(int i = 1;i <= 7;i++)
                payList.add(null);
            yaksok.put("payList", payList);

            List<HashMap<String, Object>> dateList = new ArrayList<>();
            //캘린더에서 설정한 date정보를 가져와 순회
            for (Promise promise : calendarDialog.getmPromiseList()) {
                //startTime = (Timestamp)date
                //selectCount = (Long)0
                //위와 같은 구조의 element로 리스트에 집어넣음
                HashMap<String, Object> data = new HashMap<>();
                data.put("startTime", new Timestamp(new Date(promise.getDate())));
                data.put("selectCount", 0);
                dateList.add(data);
            }
            yaksok.put("date", dateList);

            Map<String, Object> MemberData = new HashMap<>();
            MemberData.put("Kname", kako_Nickname);
            MemberData.put("Kid", kako_id);
            MemberData.put("Kprofile", kako_profile);

            yaksok.put("member1", MemberData);
            yaksok.put("fixdate", null);
            yaksok.put("membercount", 1);
            //생성자도 날짜를 선택해야 하기 때문에 selectcount는 0으로 설정
            yaksok.put("selectcount", 0);
            yaksok.put("fixed", false);
            yaksok.put("creatorid", kako_id);
            // Add a new document with a generated ID
            db.collection("Yaksok")
                    .add(yaksok)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Yaksok added with ID: " + documentReference.getId());
                            docid = documentReference.getId();
                            sqlsetyid(docid);
                            fbcreateboard(docid);
                            isfinish = true;
                            Log.d(TAG, "new docid: " + docid);

                            if(From){
                                kakaoshare();
                            }
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding Yaksok", e);
                        }
                    });
        }
    }

    private void fbcreateboard(final String docid) {
        Map<String, Object> init = new HashMap<>();
        init.put("count", 1);
        init.put("m1", kako_Nickname + "님이 약속을 생성했습니다");
        db.collection("Board").document(docid)
                .set(init)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Create board(documentid: " + docid + ") success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Create board(documentid: " + docid + ") faulure");
                    }
                });
    }



    private void sqlsetyid(String id) {
        String yid = id;
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase sqldb = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_docid", yid);
        long result = sqldb.insert("IdTabel", null, values);
        if (result == -1) {
            Log.d(TAG, "sqlsetyid() failed with id: " + yid);
        } else {
            Log.d(TAG, "sqlsetyid() success with ID: " + yid);
        }
        sqldb.close();

        ((MainActivity)MainActivity.mContext).getsqldb();
    }

    private void kakaoshare() {

        String templateId = "18884";

        Yaksok_name = ((EditText)findViewById(R.id.ysName)).getText().toString();
        Map<String, String> templateArgs = new HashMap<String, String>();
        templateArgs.put("${kakao_profile}", kako_Nickname);
        templateArgs.put("${yaksok_name}", Yaksok_name);
        templateArgs.put("${document_id}", docid);
        Log.d(TAG, "${document_id}: " + docid);

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");


        KakaoLinkService.getInstance().sendCustom(this, templateId, templateArgs, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
            }
        });
    }
}

