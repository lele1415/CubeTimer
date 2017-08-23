package com.chenxs.timer;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

    private boolean mIsStop = true;
    private boolean mShowWholeMin = false;
    private boolean mShowWholeSec = false;

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mHourNum = (TextView) findViewById(R.id.hour_num);
        mHourColon = (TextView) findViewById(R.id.hour_colon);
        mMinNum = (TextView) findViewById(R.id.min_num);
        mMinColon = (TextView) findViewById(R.id.min_colon);
        mSecNum = (TextView) findViewById(R.id.sec_num);
        mMsNum = (TextView) findViewById(R.id.ms_num);

        final Button mResetBtn = (Button) findViewById(R.id.reset);
        final Button mStartBtn = (Button) findViewById(R.id.start);

        mStartBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mHandler.removeMessages(1);

                if (mStartBtn.getText().toString().equals("start")) {
                    mCurrentSystemTime = System.currentTimeMillis();
                    mIsStop = false;
                    startTimer();
                    mStartBtn.setText("pause");
                } else {
                    mHandler.sendEmptyMessage(0);
                    mIsStop = true;
                    stopTimer();
                    mStartBtn.setText("start");
                }

            }
        });

        mResetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mIsStop = true;
                stopTimer();

                mHourNum.setVisibility(View.GONE);
                mHourColon.setVisibility(View.GONE);
                mMinNum.setVisibility(View.GONE);
                mMinColon.setVisibility(View.GONE);

                mSecNum.setText("0");
                mMsNum.setText("000");

                mStartBtn.setText("start");

                mMsCount = 0;
                mSecCount = 0;
                mMinCount = 0;
                mHourCount = 0;
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (!mIsStop) {
                        updateView();
                    }
                    break;
                case 0:
                    break;
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

    private void updateView() {
        long mNextSystemTime = System.currentTimeMillis();

        //update ms count
        mMsCount += (int) (mNextSystemTime - mCurrentSystemTime);
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

        mCurrentSystemTime = mNextSystemTime;
    }

    private String formatNumToString(int num) {
        return String.format(Locale.getDefault(), "%d", num);
    }
}
