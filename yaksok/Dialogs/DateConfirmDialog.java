package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.yaksok.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DateConfirmDialog extends Dialog {
    String TAG = "DateConfirmDialog";
    private Context context;
    List<HashMap<String, Object>> dateObjectList;
    private List<Button> buttonList;
    private LinearLayout container;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String documentid;


    /**
     * 날짜 확정 창
     * @param context
     * @param documentid 약속 docId
     */
    public DateConfirmDialog(Context context, String documentid) {
        super(context);
        this.context = context;
        this.documentid = documentid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_date_confirm);
        //버튼을 추가하기 위한 base layout (LinearLayout)
        container = findViewById(R.id.container);
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //membercount를 가져옴
                        Long membercount = (Long) document.get("membercount");
                        //date 정보를 가져옴
                        dateObjectList = (List<HashMap<String, Object>>) document.get("date");

                        final Comparator<HashMap<String, Object>> comp = (p1, p2) -> Long.compare((Long)p2.get("selectCount"), (Long)p1.get("selectCount"));

                        //date 정보를 selectCount를 기준으로 내림차순 정렬 (많이 선택한 날짜가 먼저 오도록)
                        dateObjectList = dateObjectList.stream().sorted(comp).collect(Collectors.toList());

                        for (HashMap<String, Object> dateObject : dateObjectList) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    600,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(8, 8, 8, 8);
                            //date 하나당 버튼 하나씩 생성
                            Button button = new Button(context);
                            if(membercount == dateObject.get("selectCount")) {
                                //전 멤버가 선택한 경우 노란색으로 설정
                                button.setBackgroundColor(Color.parseColor("#FEEFEC"));
                            }
                            button.setLayoutParams(params);
                            button.setPadding(10, 0, 10, 0);
                            button.setText(loadDate(((Timestamp)dateObject.get("startTime")).toDate().getTime()) + "\n" + dateObject.get("selectCount") + "명");
                            button.setOnClickListener(v -> selectDate(dateObject));
                            //container에 버튼 추가
                            container.addView(button);
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

    /**
     * 날짜 버튼이 클릭 된 경우
     * @param dateObject
     */
    private void selectDate(HashMap<String, Object> dateObject) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //약속 날짜가 확정되었는지 확인 (누군가 이전에 날짜 확정 창에서 버튼을 눌렀는지)
                        boolean isFixed = document.get("fixdate") != null;
                        if (isFixed) {
                            //이미 확정된 경우 확정되었다고 출력
                            Toast.makeText(context, "이미 약속 날짜가 확정 되었습니다.", Toast.LENGTH_LONG).show();
                            dismiss();
                        } else {
                            //확정 되지 않은 경우 DB에 저장하고 이력 업데이트
                            saveDate(dateObject);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Toast.makeText(context, "약속 날짜가 확정 되었습니다.", Toast.LENGTH_LONG).show();
                            dismiss();
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

    /**
     * 확정 날자를 DB에 저장
     * @param dateObject
     */
    private void saveDate(HashMap<String, Object> dateObject) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Yaksok").document(documentid)
                .update("fixdate", dateObject.get("startTime"));
    }

    /**
     * dateTime을 받아서 'YYYY년 MM월 DD일' 형태의 string으로 반환
     * @param dateTime
     * @return dateString
     */
    private String loadDate(Long dateTime) {
        Calendar cal = Calendar.getInstance();
        Date d = new Date(dateTime);
        cal.setTime(d);
        String ret = (cal.get(Calendar.YEAR)) + "년 " + (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DAY_OF_MONTH) + "일";
        return ret;
    }
}
