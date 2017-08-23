package com.chenxs.timer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.os.Bundle;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ResultListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<String> resultList;
    private boolean haveResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_list);
        initDatabese();
        queryDatabase();
        if (haveResult)
            initRecyclerView();
    }
    private void initDatabese() {
        dbHelper = new DatabaseHelper(this, "Result.db", null, 1);
        dbHelper.getWritableDatabase();
    }

    private void queryDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Result", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            haveResult = true;
            resultList = new ArrayList<>();
            do {
                int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                int mint = cursor.getInt(cursor.getColumnIndex("mint"));
                int sec = cursor.getInt(cursor.getColumnIndex("sec"));
                int ms = cursor.getInt(cursor.getColumnIndex("ms"));
                resultList.add(getTimeString(hour, mint, sec, ms));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private String getTimeString(int hour, int mint, int sec, int ms) {
        StringBuilder timeStr = new StringBuilder();
        //Log.i("chenxs", "getTimeString hour="+hour+" mint="+mint+" sec="+sec+" ms="+ms);

        if (hour != 0) {
            timeStr.append(hour);
            timeStr.append(":");
            if (mint < 10)
                timeStr.append("0");
        }

        if (hour != 0 || mint != 0) {
            timeStr.append(mint);
            timeStr.append(":");
            if (sec < 10)
                timeStr.append("0");
        }

        timeStr.append(sec);
        timeStr.append(".");
        if (ms < 100) {
            timeStr.append("0");
            if (ms < 10)
                timeStr.append("0");
        }
        timeStr.append(ms);

        //Log.i("chenxs", "timeStr="+timeStr.toString());
        return timeStr.toString();
    }

    private void initRecyclerView() {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.result_list_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new ResultAdapter());
    }

    public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {

        @Override
        public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ResultListActivity.this).
                    inflate(R.layout.result_item, parent, false);
            return new ResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ResultViewHolder holder, int position)
        {
            //Log.i("chenxs", "resultList.get(position)="+resultList.get(position));
            holder.item_tv.setText(resultList.get(position));
        }

        @Override
        public int getItemCount()
        {
            return resultList.size();
        }

        class ResultViewHolder extends ViewHolder {
            TextView item_tv;
            ResultViewHolder(View view) {
                super(view);
                item_tv = (TextView) view.findViewById(R.id.result_time);
            }
        }
    }
}
