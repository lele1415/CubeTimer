package com.chenxs.timer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResultListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<String> resultList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_list);
        initDatabese();
        queryDatabase();
        initListView();
    }
    private void initDatabese() {
        dbHelper = new DatabaseHelper(this, "Result.db", null, 1);
        dbHelper.getWritableDatabase();
    }

    private void queryDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Result", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
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
        String timeStr;
        String msStr = (ms < 10) ? "00" + formatNumToString(ms)
                : (ms < 100) ? "0" + formatNumToString(ms)
                : formatNumToString(ms);
        String secStr = (sec < 10) ? "0" + formatNumToString(sec)
                : formatNumToString(sec);
        String mintStr = (mint < 10) ? "0" + formatNumToString(mint)
                : formatNumToString(mint);

        if (hour != 0) {
            timeStr = formatNumToString(hour) + ":" + mintStr + ":" + secStr + "." + msStr;
        } else {
            if (mint != 0) {
                timeStr = formatNumToString(mint) + ":" + secStr + "." + msStr;
            } else {
                timeStr = formatNumToString(sec) + "." + msStr;
            }
        }
         return timeStr;
    }

    private String formatNumToString(int num) {
        return String.format(Locale.getDefault(), "%d", num);
    }

    private void initListView() {
        resultAdapter resultAdapter = new resultAdapter(ResultListActivity.this,
                R.layout.result_item, resultList);
        ListView listView = (ListView) findViewById(R.id.result_list_activity);
        listView.setAdapter(resultAdapter);
    }

    public class resultAdapter extends ArrayAdapter<String> {
        private int resourceId;

        public resultAdapter (Context context, int textViewResourceId,
                              List<String> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String timeStr = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            }

            TextView resultTime = (TextView) convertView.findViewById(R.id.result_time);
            resultTime.setText(timeStr);
            return convertView;
        }
    }
}
