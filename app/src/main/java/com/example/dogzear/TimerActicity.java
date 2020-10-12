package com.example.dogzear;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogzear.dto.BookItem;
import com.example.dogzear.saveAndLoad.SaveAndLoad;

import java.util.ArrayList;

public class TimerActicity extends AppCompatActivity {
    private static final String TAG = "TimerActicity";
    private static final String SETTINGS_BOOK_JSON = "settings_book_json";

    BookItem item;
    //각 책에 할당된 메모들의 리스트
    private ArrayList<BookItem> listViewItemList;
    String bookId;
    //스톱워치의 시간을 저장할 변수
    long recordTime;
    int originPosition;

    TextView mEllapse;
    TextView mSplit;
    Button mBtnStart;
    Button mBtnSplit;

    //스톱워치의 상태를 위한 상수

    final static int IDLE = 0;
    final static int RUNNING = 1;
    final static int PAUSE = 2;

    int mStatus = IDLE;//처음 상태는 IDLE
    long mBaseTime;
    long mPauseTime;
    int mSplitCount = 1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);


        mEllapse = (TextView)findViewById(R.id.ellapse);
        mSplit = (TextView)findViewById(R.id.split);
        mBtnStart = (Button)findViewById(R.id.btnstart);
        mBtnSplit = (Button)findViewById(R.id.btnsplit);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        listViewItemList = SaveAndLoad.getStringArrayPref(this, SETTINGS_BOOK_JSON);

        //bookId가 같은 인덱스를 구한다. -> 필터되지 않는 ArrayList에서의 인덱스를 구한다. ****
        for(int i = 0; i < listViewItemList.size(); i++) {
            if(listViewItemList.get(i).getBookId().equals(bookId)) {
                originPosition = i;
            }
        }

        item = listViewItemList.get(originPosition);

        //책에 저장된 독서시간이 있으면 String값을 가져와 Long으로 변환한후 형식에 맞게 String.format으로 변형시켜준다.
        if(!item.getReadingTime().equals("")) {
            recordTime = Long.parseLong(item.getReadingTime());
            String recordTimeStr = String.format("%02d:%02d:%02d", recordTime / 1000 / 60 / 60 , recordTime / 1000 / 60, (recordTime/1000)%60);
            mEllapse.setText(recordTimeStr);
        }

    }

    //스톱워치는 위해 핸들러를 만든다.
    Handler mTimer = new Handler(){

        //핸들러는 기본적으로 handleMessage에서 처리한다.
        public void handleMessage(android.os.Message msg) {
            //텍스트뷰를 수정해준다.
            mEllapse.setText(getEllapse());
            //메시지를 다시 보낸다.
            mTimer.sendEmptyMessage(0);//0은 메시지를 구분하기 위한 것
        };
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mTimer.removeMessages(0);//메시지를 지워서 메모리릭 방지
        super.onDestroy();
    }

    public void mOnClick(View v){
        switch(v.getId()){

            //시작 버튼이 눌리면
            case R.id.btnstart:
                switch(mStatus){
                    //IDLE상태이면
                    case IDLE:
                        //현재 값을 세팅해주고
                        mBaseTime = SystemClock.elapsedRealtime();
                        //현재시간을 기준으로 나중에 지난 시간을 계산해 주는 방식이므로
                        //저장한 시간부터 시작하려면 현재시간에서 내가 저장한 시간만큼 빼준다.
                        mBaseTime -= recordTime;
                        //핸드러로 메시지를 보낸다
                        mTimer.sendEmptyMessage(0);
                        //시작을 중지로 바꾸고
                        mBtnStart.setText("중지");
                        //옆버튼의 Enable을 푼 다음
                        mBtnSplit.setEnabled(true);
                        //상태를 RUNNING으로 바꾼다.
                        mStatus = RUNNING;
                        break;

                    //버튼이 실행상태이면
                    case RUNNING:
                        //핸들러 메시지를 없애고
                        mTimer.removeMessages(0);
                        //멈춘 시간을 파악
                        mPauseTime = SystemClock.elapsedRealtime();

                        recordTime = getEllapseRaw();
                        Log.d(TAG, "스톱워치에 나타난 시간 - 저장할 시간 : " + Long.toString(recordTime));
                        item.setReadingTime(Long.toString(recordTime));
                        //버튼 텍스트를 바꿔줌
                        mBtnStart.setText("시작");
                        mBtnSplit.setText("초기화");
                        mStatus = PAUSE;//상태를 멈춤으로 표시
                        break;

                    //멈춤이면
                    case PAUSE:
                        //현재값 가져옴
                        long now = SystemClock.elapsedRealtime();
                        //베이스타임 = 베이스타임 + (now - mPauseTime)
                        //잠깐 스톱워치를 멈췄다가 다시 시작하면 기준점이 변하게 되므로..
                        mBaseTime += (now - mPauseTime);

                        mTimer.sendEmptyMessage(0);

                        //텍스트 수정
                        mBtnStart.setText("중지");
                        mBtnSplit.setText("기록");
                        mStatus = RUNNING;
                        break;
                }
                break;

            case R.id.btnsplit:
                switch(mStatus){
                    //RUNNING 상태일 때.
                    case RUNNING:

                        //기존의 값을 가져온뒤 이어붙이기 위해서//todo 묶어서 하나로 수정
                        String sSplit = mSplit.getText().toString();

                        //+연산자로 이어붙임
                        sSplit += String.format("%d 번째 기록   :   %s\n", mSplitCount, getEllapse());

                        //텍스트뷰의 값을 바꿔줌
                        mSplit.setText(sSplit);
                        mSplitCount++;
                        break;

                    case PAUSE://여기서는 초기화버튼이 됨
                        //핸들러를 없애고
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("북 타이머").setMessage("타이머를 초기화 하시겠습니까?\n초기화하면 저장된 시간이 지워집니다.");
                        builder.setPositiveButton("예", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                mTimer.removeMessages(0);

                                //처음상태로 원상복귀시킴
                                mBtnStart.setText("시작");
                                mBtnSplit.setText("기록");
                                mEllapse.setText("00:00:00");
                                mStatus = IDLE;
                                mSplit.setText("");
                                mBtnSplit.setEnabled(false);
                            }
                        });
                        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            { Toast.makeText(getApplicationContext(), "Cancel Click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        break;
                }
                break;
        }
    }

    String getEllapse(){

        long now = SystemClock.elapsedRealtime();
        long ell = now - mBaseTime;//현재 시간과 지난 시간을 빼서 ell값을 구하고//todo 이해할 수 있게 변수명 사용
        //아래에서 포맷을 예쁘게 바꾼다음 리턴해준다.
        String sEll = String.format("%02d:%02d:%02d", ell / 1000 / 60 / 60 , ell / 1000 / 60, (ell/1000)%60);
        return sEll;
    }

    long getEllapseRaw() {
        long now = SystemClock.elapsedRealtime();
        long ell = now - mBaseTime;//현재 시간과 지난 시간을 빼서 ell값을 구하고

        return ell;
    }

    @Override
    public void onBackPressed() {
        //뒤로가기로 나갈시 시간을 저장한다.
        SaveAndLoad.setStringArrayPref(this,SETTINGS_BOOK_JSON, listViewItemList);
        super.onBackPressed();


    }
}
