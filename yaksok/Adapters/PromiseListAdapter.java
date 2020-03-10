package com.example.yaksok.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.yaksok.Models.Promise;
import com.example.yaksok.R;

import java.util.ArrayList;

public class PromiseListAdapter extends BaseAdapter implements View.OnClickListener {
    private LayoutInflater inflater = null;
    private ArrayList<Promise> promiseArrayList;

    private PromiseListClickListener promiseListClickListener;

    /*
     *리스트에서 DEL 버튼을 눌렀을 때의 리스트에서 위치를 판별
     */
    public PromiseListAdapter(ArrayList<Promise> promiseArrayList, PromiseListClickListener promiseListClickListener) {
        this.promiseArrayList = promiseArrayList;
        this.promiseListClickListener = promiseListClickListener;
    }

    @Override
    public int getCount() {
        return promiseArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            final Context context = parent.getContext();
            if(inflater == null) inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.listview_date_item, parent, false);
        }

        TextView textDate = convertView.findViewById(R.id.txt_date);

        Button deleteButton = convertView.findViewById(R.id.btn_delete);
        deleteButton.setTag(position);
        deleteButton.setOnClickListener(this);

        textDate.setText(promiseArrayList.get(position).getDateString());
        return convertView;
    }


    public interface PromiseListClickListener {
        void onListButtonClick(int position);
    }

    @Override
    public void onClick(View v) {
        if(this.promiseListClickListener != null) {
            this.promiseListClickListener.onListButtonClick((int)v.getTag());
        }
    }
}
