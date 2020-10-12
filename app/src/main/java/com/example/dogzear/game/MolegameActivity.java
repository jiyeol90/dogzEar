package com.example.dogzear.game;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogzear.R;

public class MolegameActivity extends AppCompatActivity {
    private Handler mHandler = null;
    int mScore = 0;
    int mTime = 30;
    int[] imgValue = new int[9];
    ImageButton[] imgMole = new ImageButton[9];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mole);

        Button btnReset = (Button)findViewById(R.id.btnReset);
        final TextView txtScore = (TextView)findViewById(R.id.txtScore);
        final TextView  txtTime = (TextView)findViewById(R.id.txtTime);
        imgMole[0] = (ImageButton)findViewById(R.id.imageButton1);
        imgMole[1] = (ImageButton)findViewById(R.id.imageButton2);
        imgMole[2] = (ImageButton)findViewById(R.id.imageButton3);
        imgMole[3] = (ImageButton)findViewById(R.id.imageButton4);
        imgMole[4] = (ImageButton)findViewById(R.id.imageButton5);
        imgMole[5] = (ImageButton)findViewById(R.id.imageButton6);
        imgMole[6] = (ImageButton)findViewById(R.id.imageButton7);
        imgMole[7] = (ImageButton)findViewById(R.id.imageButton8);
        imgMole[8] = (ImageButton)findViewById(R.id.imageButton9);
        btnReset.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mScore = 0;
                mTime = 30;
                mHandler.sendEmptyMessageDelayed(0, 1000);
            }
        });
        for(int i=0; i<9; i++){
            final int arrayI = i;
            imgMole[i].setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    imgMole[arrayI].setImageResource(R.drawable.none);
                    if(imgValue[arrayI] == 1){
                        mScore++;
                        imgValue[arrayI] =0;
                    }

                }
            });
        }
        mHandler = new Handler() {

            public void handleMessage(Message msg) {

                txtTime.setText("시간:" + mTime);
                txtScore.setText("점수:" + mScore);
                if(mTime== 0){
                    Toast.makeText(getBaseContext(),
                            "종료", Toast.LENGTH_LONG).show();

                    return;
                }
                mHandler.sendEmptyMessageDelayed(0, 1000);
                mTime--;
                double rValue = Math.random();
                int selectedIndex = (int)(rValue * 10);
                if(selectedIndex == 9)
                    selectedIndex = 4;
                for(int i=0; i<9;i++){
                    imgValue[i] = 0;
                    imgMole[i].setImageResource(R.drawable.none);
                }
                imgValue[selectedIndex] = 1;
                imgMole[selectedIndex].setImageResource(R.drawable.mole);
            }
        };
    }
}