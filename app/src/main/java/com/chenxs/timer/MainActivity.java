package com.chenxs.timer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView mHourNumView;
    private TextView mHourColonView;
    private TextView mMintNumView;
    private TextView mMintColonView;
    private TextView mSecNumView;
    private TextView mMsNumView;

    private int mMsCount = 0;
    private int mSecCount = 0;
    private int mMintCount = 0;
    private int mHourCount = 0;

    private boolean mMintShow = false;
    private boolean mHourShow = false;

    private StringBuilder msStr = new StringBuilder();
    private StringBuilder secStr = new StringBuilder();
    private StringBuilder mintStr = new StringBuilder();
    private StringBuilder hourStr = new StringBuilder();

    private long mCurrentSystemTime;

    private boolean mIsRunning = false;
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
        mHourNumView = (TextView) findViewById(R.id.hour_num);
        mHourColonView = (TextView) findViewById(R.id.hour_colon);
        mMintNumView = (TextView) findViewById(R.id.min_num);
        mMintColonView = (TextView) findViewById(R.id.min_colon);
        mSecNumView = (TextView) findViewById(R.id.sec_num);
        mMsNumView = (TextView) findViewById(R.id.ms_num);

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
        //Log.i("chenxs", "addResultToDatabase hour="+hour+" mint="+mint+" sec="+sec+" ms="+ms);
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
                        addResultToDatabase(mHourCount, mMintCount, mSecCount, mMsCount);
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
        }
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
        mHourNumView.setVisibility(View.GONE);
        mHourColonView.setVisibility(View.GONE);
        mMintNumView.setVisibility(View.GONE);
        mMintColonView.setVisibility(View.GONE);

        mSecNumView.setText("0");
        mMsNumView.setText("000");

        mMsCount = 0;
        mSecCount = 0;
        mMintCount = 0;
        mHourCount = 0;

        mMintShow = false;
        mHourShow = false;
    }

    private void updateView() {
        long mDiffTime = System.currentTimeMillis() - mCurrentSystemTime;
        mCurrentSystemTime += mDiffTime;
        updateMsCount(mDiffTime);
    }

    private void updateMsCount(long plusNum) {
        mMsCount += (int) plusNum;
        if (mMsCount > 999) {
            mSecCount += mMsCount / 1000;
            updateSecCount();
            mMsCount = mMsCount % 1000;
        }

        msStr.delete(0, msStr.length());
        msStr.append(mMsCount);
        if (mMsCount < 100) {
            msStr.insert(0, "0");
            if (mMsCount < 10) {
                msStr.insert(0, "0");
            }
        }

        mMsNumView.setText(msStr);
    }

    private void updateSecCount() {
        if (mSecCount > 59) {
            mMintCount += mSecCount / 60;
            updateMintCount();
            mSecCount = mSecCount % 60;
        }

        secStr.delete(0, secStr.length());
        secStr.append(mSecCount);
        if (mSecCount < 10 && mMintShow) {
            secStr.insert(0, "0");
        }

        mSecNumView.setText(secStr);
    }

    private void updateMintCount() {
        if (mMintCount > 59) {
            mHourCount += mMintCount / 60;
            updateHourCount();
            mMintCount = mMintCount % 60;
        }

        mintStr.delete(0, mintStr.length());
        mintStr.append(mMintCount);
        if (mMintCount < 10 && mHourShow) {
            mintStr.insert(0, "0");
        }

        if (!mMintShow) {
            mMintNumView.setVisibility(View.VISIBLE);
            mMintColonView.setVisibility(View.VISIBLE);
            mMintShow = true;
        }

        mMintNumView.setText(mintStr);
    }

    private void updateHourCount() {
        hourStr.delete(0, hourStr.length());
        hourStr.append(mHourCount);

        if (!mHourShow) {
            mHourNumView.setVisibility(View.VISIBLE);
            mHourColonView.setVisibility(View.VISIBLE);
            mHourShow = true;
        }

        mHourNumView.setText(hourStr);
    }
}
