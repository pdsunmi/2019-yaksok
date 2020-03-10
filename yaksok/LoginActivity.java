package com.example.yaksok;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ApiErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    String TAG = "LoginActivity";
    String Creditid;
    String documentid;
    String kakaoname;
    Boolean isfixed;

    //카카오 로그인 콜백 선언
    private SessionCallback sessionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //카카오 로그인 콜백 초기화
        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        //앱 실행 시 로그인 토큰이 있으면 자동으로 로그인 수행
        Session.getCurrentSession().checkAndImplicitOpen();

        //커스텀 카카오 로그인 버튼
        Button btnLoginKakao = findViewById(R.id.kakaoLoginButton);
        btnLoginKakao.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //카카오 로그인 화면에서 값이 넘어온 경우 처리
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        //Activity Destroy 시 카카오 로그인 콜백 제거
        //이 코드가 없으면 타 로그인 플랫폼과 연동 시 오류가 발생할 가능성이 높다.
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    //카카오 로그인 콜백
    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() { //세션이 성공적으로 열린 경우
            UserManagement.getInstance().me(new MeV2ResponseCallback() { //유저 정보를 가져온다.
                @Override
                public void onFailure(ErrorResult errorResult) { //유저 정보를 가져오는 데 실패한 경우
                    int result = errorResult.getErrorCode(); //오류 코드를 받아온다.

                    if (result == ApiErrorCode.CLIENT_ERROR_CODE) { //클라이언트 에러인 경우: 네트워크 오류
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else { //클라이언트 에러가 아닌 경우: 기타 오류
                        Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) { //세션이 도중에 닫힌 경우
                    Toast.makeText(getApplicationContext(), "세션이 닫혔습니다. 다시 시도해 주세요: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) { //유저 정보를 가져오는데 성공한 경우
                    Long kakaoid = result.getId();
                    kakaoname = result.getNickname();
                    Creditid = kakaoid.toString();
                    Intent gintent = getIntent();
                    documentid = gintent.getStringExtra("documentid");
                    if(documentid != null){
                        Log.d(TAG, "get documentid from SplashActivity");
                        fbisfixed(documentid);
                    }
                    checkid(Creditid);

                    //MainActivity로 넘어가면서 유저 정보를 같이 넘겨줌
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("name", kakaoname); //유저 이름(String)
                    intent.putExtra("profile", result.getProfileImagePath()); //유저 프로필 사진 주소(String)
                    intent.putExtra("Credit", Creditid);
                    //intent.putExtra("fixed", isfixed);
                    //Log.d(TAG, "firebase fixed: " + isfixed);
                    intent.putExtra("documentid", documentid);


                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) { //세션을 여는 도중 오류가 발생한 경우 -> Toast 메세지를 띄움.
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
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
                        if(!isfixed){
                            sqlsetid(documentid);
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

    private void checkid(String creditid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Credit").document(creditid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.d(TAG, "id already exists in firebase.");
                    } else {
                        //Log.d(TAG, "No such document. Let's Create.");
                        fbsetid(Creditid);
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void fbsetid(String kakaoid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a first, middle, and last name
        Map<String, Object> CreditObj = new HashMap<>();
        CreditObj.put("score", 70);
        CreditObj.put("isChanged", false);
        CreditObj.put("name", null);
        CreditObj.put("profile", null);

        // Add a new document with a generated ID
        db.collection("Credit").document(kakaoid)
                .set(CreditObj)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoidVoid) {
                        //Log.d(TAG, "firestore set credit");
                    }
                });
    }

    private void sqlsetid(String documentid) {
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase sqldb = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_docid", documentid);
        try {
            long result = sqldb.insertOrThrow("IdTabel", null, values);
            if (result != -1) {
                Log.d(TAG, "sqlsetyid() success with ID: " + documentid);
            }
        } catch (SQLiteException se) {
            Log.d(TAG, "sqlsetyid() failed with id: " + documentid + ". It's already exist");
        }
        sqldb.close();
    }
}