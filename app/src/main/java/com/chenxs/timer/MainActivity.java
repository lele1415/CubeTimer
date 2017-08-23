package com.chenxs.timer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView mHourNum;
    private TextView mHourColon;
    private TextView mMinNum;
    private TextView mMinColon;
    private TextView mSecNum;
    private TextView mMsNum;

    private int mMsCount = 0;
    private int mSecCount = 0;
    private int mMinCount = 0;
    private int mHourCount = 0;

    private long mCurrentSystemTime;

    private boolean mIsRunning = false;
    private boolean mShowWholeMin = false;
    private boolean mShowWholeSec = false;
    private boolean isTouchHandled = false;

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initDatabese();
    }

    private void initViews() {
        mHourNum = (TextView) findViewById(R.id.hour_num);
        mHourColon = (TextView) findViewById(R.id.hour_colon);
        mMinNum = (TextView) findViewById(R.id.min_num);
        mMinColon = (TextView) findViewById(R.id.min_colon);
        mSecNum = (TextView) findViewById(R.id.sec_num);
        mMsNum = (TextView) findViewById(R.id.ms_num);

        final RelativeLayout ll = (RelativeLayout) findViewById(R.id.activity_main);
        ll.setOnTouchListener(new TouchListener());

        Button queryBtn = (Button) findViewById(R.id.result_list_button);
        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ResultListActivity.class);
                startActivity(i);
            }
        });
    }

    private void initDatabese() {
        dbHelper = new DatabaseHelper(this, "Result.db", null, 1);
        dbHelper.getWritableDatabase();
    }

    private void addResultToDatabase(int hour, int mint, int sec, int ms) {
        Log.i("chenxs", "addResultToDatabase hour="+hour+" mint="+mint+" sec="+sec+" ms="+ms);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hour", hour);
        values.put("mint", mint);
        values.put("sec", sec);
        values.put("ms", ms);
        db.insert("Result", null, values);
        values.clear();
    }

    class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eventaction = event.getAction();
            switch (eventaction) {
                case MotionEvent.ACTION_DOWN:
                    if (mIsRunning) {
                        mIsRunning = false;
                        stopTimer();
                        addResultToDatabase(mHourCount, mMinCount, mSecCount, mMsCount);
                        isTouchHandled = true;
                    } else {
                        isTouchHandled = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mIsRunning && !isTouchHandled) {
                        mCurrentSystemTime = System.currentTimeMillis();
                        mIsRunning = true;
                        resetTime();
                        startTimer();
                    }
                    break;
            }
            return true;
        };
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (mIsRunning) {
                updateView();
            }
        }

    };

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(1);
                }
            };
        }

        mTimer.schedule(mTimerTask, 1, 1);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private void resetTime() {
        mHourNum.setVisibility(View.GONE);
        mHourColon.setVisibility(View.GONE);
        mMinNum.setVisibility(View.GONE);
        mMinColon.setVisibility(View.GONE);

        mSecNum.setText("0");
        mMsNum.setText("000");

        mMsCount = 0;
        mSecCount = 0;
        mMinCount = 0;
        mHourCount = 0;

        mShowWholeMin = false;
        mShowWholeSec = false;
    }

    private void updateView() {
        long mDiffTime = System.currentTimeMillis() - mCurrentSystemTime;
        mCurrentSystemTime += mDiffTime;

        //update ms count
        mMsCount += (int) mDiffTime;
        if (mMsCount > 999) {
            mMsCount = mMsCount % 1000;

            //update sec count
            mSecCount += 1;
            if (mSecCount > 59) {
                mShowWholeSec = true;
                mSecCount = mSecCount % 60;

                //update min count
                mMinCount += 1;
                if (mMinCount > 59) {
                    mShowWholeMin = true;
                    mMinCount = mMinCount % 60;

                    //update hour count
                    mHourCount += 1;
                    //update hour view
                    mHourNum.setVisibility(View.VISIBLE);
                    mHourColon.setVisibility(View.VISIBLE);
                    mHourNum.setText(formatNumToString(mHourCount));
                }

                //update min view
                mMinNum.setVisibility(View.VISIBLE);
                mMinColon.setVisibility(View.VISIBLE);
                mMinNum.setText((mShowWholeMin && mMinCount < 10) ? "0" + formatNumToString(mMinCount) :
                        formatNumToString(mMinCount));
            }

            //update sec view
            mSecNum.setText((mShowWholeSec && mSecCount < 10) ? "0" + formatNumToString(mSecCount) :
                    formatNumToString(mSecCount));
        }

        //update ms view
        mMsNum.setText(mMsCount < 10 ? "00" + formatNumToString(mMsCount) :
                mMsCount < 100 ? "0" + formatNumToString(mMsCount) :
                        formatNumToString(mMsCount));
    }

    private String formatNumToString(int num) {
        return String.format(Locale.getDefault(), "%d", num);
    }
}
