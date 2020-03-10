package com.example.yaksok.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.yaksok.Adapters.PromiseListAdapter;
import com.example.yaksok.Models.Promise;
import com.example.yaksok.Models.PromiseTime;
import com.example.yaksok.R;
import com.example.yaksok.Utils.EventDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class CalendarDialog extends Dialog implements OnDateSelectedListener, OnMonthChangedListener, View.OnClickListener, PromiseListAdapter.PromiseListClickListener{

    private MaterialCalendarView materialCalendarView;
    private Context context;

    private SQLiteDatabase promiseDB = null;
    private final String dbName = "yaksok";
    private final String tableName = "promise";

    private ArrayList<Promise> mPromiseList;
    private ListView listView;
    private Button buttonDateConfirm;
    private PromiseListAdapter adapter;

    //날짜 확정 버튼 리스너
    public interface ConfirmListener {
        void onConfirm();
    }

    /**
     데이터베이스에 올리기전에 변하는 상황을 저장
     */
    private ArrayList<PromiseTime> preAddPromiseList, preDelPromiseList;
    private ImageButton closeButton;
    private ImageButton checkButton;
    private String creatorid, kakaoid;
    private boolean isFixable;
    private ConfirmListener confirmListener;

    /**
     *
     * @param context
     * @param isFixable: 날짜 확정 버튼 전시 여부
     */
    public CalendarDialog(Context context, boolean isFixable, String creatorid, String kakaoid) {
        super(context);
        this.context = context;
        this.isFixable = isFixable;
        this.creatorid = creatorid;
        this.kakaoid = kakaoid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_calendar);

        promiseDB = context.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
        init_table();

        listView = findViewById(R.id.list_promise);
        buttonDateConfirm = findViewById(R.id.btn_dateConfirm);
        buttonDateConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //날짜 확정 버튼이 눌렸을 경우 콜백 함수 호출
                dismiss();
                if(confirmListener != null) {
                    confirmListener.onConfirm();
                }
            }
        });
        mPromiseList = new ArrayList<>();
        preAddPromiseList = new ArrayList<>();
        preDelPromiseList = new ArrayList<>();
        materialCalendarView = findViewById(R.id.calendarView);
        if (isFixable && creatorid.equals(kakaoid)) {
            //날짜 확정 버튼 전시 여부가 참일 경우 전시되도록 설정
            buttonDateConfirm.setVisibility(View.VISIBLE);
        }

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2018, 0, 1))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        materialCalendarView.setOnDateChangedListener(this);
        materialCalendarView.setOnMonthChangedListener(this);
        getPromiseData(materialCalendarView.getCurrentDate().getDate());

        /**
         * 버튼 클릭 리스너 등록
         */
        closeButton = findViewById(R.id.btn_close);
        checkButton = findViewById(R.id.btn_check);
        closeButton.setOnClickListener(this);
        checkButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            /**
             * 바로 종료
             */
            case R.id.btn_close:
                this.dismiss();
                break;
            case R.id.btn_check:
                /**
                 * 변화된 데이터를 반영해야 하기 때문에
                 * 데이터베이스에 추가,제거.
                 */
                insertPromiseData();
                deletePromiseData();
                this.dismiss();
                break;
        }
    }

    /**
     * 테이블이 없는 경우 생성
     */
    private void init_table() {
        if (promiseDB != null) {
            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS " + tableName +
                    "(id " + "INTEGER, " +
                    "date " + "DATE)";
            promiseDB.execSQL(sqlCreateTbl);
        }
    }

    /**
     * 저장해두었던, 추가된 목록을 데이터베이스에 추가
     */
    private void insertPromiseData() {
        for(PromiseTime add : preAddPromiseList) {
            promiseDB.execSQL("INSERT INTO " + tableName +
                    "(id, date) VALUES ('" + add.getId() + "', '" + add.getDate() + "');");
        }
    }

    /**
     * 저장해두었던, 제거된 목록을 데이터베이스에서 제거
     */
    private void deletePromiseData() {
        for(PromiseTime del : preDelPromiseList) {
            promiseDB.execSQL("DELETE FROM " + tableName +
                    " WHERE id = '" + del.getId() + "';");
        }
    }
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull final CalendarDay date, boolean selected) {
        int id = (mPromiseList.size() == 0) ? 99 : mPromiseList.get(mPromiseList.size() - 1).getId() + 100;
        preAddPromiseList.add(new PromiseTime(id, date.getCalendar().getTimeInMillis()));
        mPromiseList.add(new Promise(id, date.getCalendar().getTimeInMillis(), loadDate(date.getCalendar().getTimeInMillis())));
        adapter = new PromiseListAdapter(mPromiseList, CalendarDialog.this);
        listView.setAdapter(adapter);
    }

    /**
     * DB에서 선택한 Date 정보들을 불러옴
     */
    private void getPromiseData(Date mDate) {
        mPromiseList.clear();
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR, 0); cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.MONTH, 1);

        try {
            ArrayList <CalendarDay> dates = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            //DB에서 오름차순으로 date 정보들을 가져옴
            Cursor c = promiseDB.rawQuery("SELECT * FROM " + tableName + " ORDER BY " + "date " + "ASC" + ";", null);
            if(c != null && c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex("id"));
                    long date = c.getLong(c.getColumnIndex("date"));
                    Date d = new Date(c.getLong(c.getColumnIndex("date")));
                    calendar.setTime(d);
                    CalendarDay day = CalendarDay.from(calendar);
                    dates.add(day);

                    mPromiseList.add(new Promise(id, date, loadDate(date)));
                } while(c.moveToNext());
            }
            materialCalendarView.removeDecorators();
            materialCalendarView.addDecorator(new EventDecorator(10, 0xFFFF6600, dates, getOwnerActivity()));
            adapter = new PromiseListAdapter(mPromiseList, this);
            listView.setAdapter(adapter);
        } catch (SQLiteException se) {
            Toast.makeText(context, se.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 전달된 dateTime을 'MM월 DD일' 형식의 string으로 반환
     * @param dateTime
     * @return dateString
     */
    private String loadDate(Long dateTime) {
        Calendar cal = Calendar.getInstance();
        Date d = new Date(dateTime);
        cal.setTime(d);
        String ret = (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DAY_OF_MONTH) + "일";
        return ret;
    }

    /**
     * DEL 버튼을 눌렀을 시에 제거될 목록에 추가
     * 변화된 리스트를 반영
     */
    @Override
    public void onListButtonClick(int position) {
        preDelPromiseList.add(new PromiseTime(mPromiseList.get(position).getId(), 1));
        mPromiseList.remove(position);
        adapter = new PromiseListAdapter(mPromiseList, this);
        listView.setAdapter(adapter);
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

    }

    public ArrayList<Promise> getmPromiseList() {
        return mPromiseList;
    }

    /**
     * 날짜 확정 이벤트 리스너 등록
     * @param listener
     */
    public void setConfirmListener(ConfirmListener listener) {
        this.confirmListener = listener;
    }
}
