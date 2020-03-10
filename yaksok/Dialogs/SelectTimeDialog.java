package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Date;

public class SelectTimeDialog extends Dialog implements View.OnClickListener{
    String TAG = "SelectTimeRangeDialog";
    private Context context;
    private TimePicker timePickerStart;
    private Button btnComplete;
    private SelectTimeDialogListener selectTimeDialogListener;
    private String documentid;
    private TextView dateTextView;
    public SelectTimeDialog(@NonNull Context context, String documentid) {
        super(context);
        this.context = context;
        this.documentid = documentid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_select_time);
        timePickerStart = findViewById(R.id.timePicker_start);
        btnComplete = findViewById(R.id.btn_select_time);
        dateTextView = findViewById(R.id.tv_date);

        btnComplete.setOnClickListener(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Yaksok").document(documentid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //약속 날짜와 시간이 이미 확정되었는지 확인 (누군가 이전에 날짜 확정 창에서 버튼을 눌렀는지)
                        Timestamp date = (Timestamp) document.get("fixdate");
                        dateTextView.setText(loadDate(date.toDate().getTime()));
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
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_select_time:

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("Yaksok").document(documentid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //약속 날짜와 시간이 이미 확정되었는지 확인 (누군가 이전에 날짜 확정 창에서 버튼을 눌렀는지)
                                boolean isFixed = document.get("fixdatetime") != null;
                                if (isFixed) {
                                    //이미 확정된 경우 확정되었다고 출력
                                    Toast.makeText(context, "이미 약속 날짜가 확정 되었습니다.", Toast.LENGTH_LONG).show();
                                    dismiss();
                                } else {
                                    //DB에 설정된 date 정보 가져옴
                                    Timestamp fixdate = (Timestamp) document.get("fixdate");
                                    Date date = fixdate.toDate();
                                    Calendar cal = Calendar.getInstance();
                                    //date에 시간을 세팅함
                                    cal.setTime(date);
                                    cal.set(Calendar.HOUR_OF_DAY,timePickerStart.getHour());
                                    cal.set(Calendar.MINUTE,timePickerStart.getMinute());
                                    cal.set(Calendar.SECOND,0);
                                    cal.set(Calendar.MILLISECOND,0);
                                    //확정 되지 않은 경우 DB에 저장하고 이력 업데이트
                                    saveDate(cal.getTime());
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
                dismiss();
                break;
        }
    }

    /**
     * 확정 날자를 DB에 저장
     * @param dateObject
     */
    private void saveDate(Date dateObject) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Yaksok").document(documentid)
                .update("fixdatetime", new Timestamp(dateObject));
    }

    public void setSelectTimeDialogListener(SelectTimeDialogListener selectTimeDialogListener) {
        this.selectTimeDialogListener = selectTimeDialogListener;
    }
    public interface SelectTimeDialogListener {
        void onCompleteListener(int startHour, int startMin, int endHour, int endMin);
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
