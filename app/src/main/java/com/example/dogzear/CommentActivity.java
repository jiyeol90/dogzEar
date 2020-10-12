package com.example.dogzear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.lottie.LottieAddBookDialogFragment;
import com.example.dogzear.lottie.LottieCompleteBookDialogFragment;
import com.example.dogzear.lottie.LottieDeleteBookDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommentActivity extends BaseActivity implements View.OnClickListener, RatingBar.OnRatingBarChangeListener{
    private static final String TAG = "CommentActivity";
    //별점 평가
    private RatingBar ratingBar;
    private Button completeBtn;
    private EditText commentEditText;
    private TextView ratingPoint;
    private Handler mhandler;
    //저장할 점수
    private String bookRating = "";
    private String bookComment = "";

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //별점으로 평가하기 위한 RatingBar
        ratingBar = (RatingBar) findViewById(R.id.search_bar);
        //완료버튼 -> 누르면 별점의 점수와 리뷰. 그리고 기존의 읽기 시작한 날짜는 완독한날짜로 대체되어 저장된다.
        completeBtn = (Button) findViewById(R.id.complete_btn);
        //리뷰내용을 적는다.
        commentEditText = (EditText) findViewById(R.id.comment);
        //별점을 선택할때 선택한 후에 점수가 나오는 방식이 아니라 별점의 점수가 실시간으로 반영되어 나타내어진다. -> 네이버 웹툰 별점 기능
        ratingPoint = (TextView) findViewById(R.id.rating_point);

        ratingBar.setOnRatingBarChangeListener(this);
        completeBtn.setOnClickListener(this);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        Toast.makeText(this, (position + 1) + "번째 책의 독서를 완료했습니다.", Toast.LENGTH_SHORT).show();

        //핸들러를 사용해 Runnable객체를 보내기 위해 수신 스레드에서 객체를 생성한다.
        //출처: https://recipes4dev.tistory.com/170?category=768056
        final Handler handler = new Handler(Looper.getMainLooper());


        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                ratingPoint.setText(Float.toString(ratingBar.getRating()));
            }
        };

        //핸들러에 담을 실행코드가 담긴 객체 : Runnable객체
        class RatingRunnable implements Runnable {

            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace() ;
                    }
                    handler.post(runnable);
                }
            }
        }

        RatingRunnable ratingRunnable = new RatingRunnable();
        Thread readPointThread = new Thread(ratingRunnable);
        readPointThread.start();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.complete_btn:
                //작성 완료하면 작성하나 날짜로 독서완료 날짜(finishDate)를 자동으로 저장하기
                //현재 시간 구하기
                long now = System.currentTimeMillis();
                //현재 시간을 날짜에 저장
                Date date = new Date(now);
                //달력에 찍힌 점 확인하는 작업 (현재날짜로 저장하기때문에 일부러 변형을 시킨다.)
                //Todo 발표시에 수정할것
                int randomDate = (int)((Math.random() * 20) + 1);
                date.setDate(randomDate);
                //날짜,시간을 나타내려고 하는 포맷 지정(앱에서는 yyyy/mm/dd 형식)
                SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/MM/dd");
                String finishDate = mFormat.format(date);
                Log.d(TAG,"현재시간을 가져온다. 이렇게 > " + finishDate);

                bookComment = commentEditText.getText().toString();
                Intent resultIntent = new Intent();
                if(bookRating.equals("")) {
                    bookRating = "0";
                }
                resultIntent.putExtra("rating", bookRating);
                resultIntent.putExtra("comment", bookComment);
                resultIntent.putExtra("position", position);
                resultIntent.putExtra("finishDate", finishDate);
                resultIntent.putExtra("RESULT", "COMPLETE");

                setResult(RESULT_OK, resultIntent);
                showProgressDialog();
                mhandler = new Handler(Looper.getMainLooper());
                new CompleteBookLoadingThread().start();
                //finish();
                break;
        }
    }

    //로딩 애니메이션 화면을 보여주기위해 finish()의 호출을 Thread.sleep()의 시간만큼 지연시킨다.
    class CompleteBookLoadingThread extends Thread {
        Handler handler = mhandler ;

        @Override
        public void run() {
            // create Runnable instance.
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            };
            try {
                Thread.sleep(1500);
                // send runnable object.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(runnable) ;
        }

    }

    //별점이 바뀌었을대의 이벤트
    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        ratingBar.setRating(rating);
        //점수(rating)를 bookItem 객체에서 String 타입으로 설정해놓았으므로 주의한다.
        bookRating = Float.toString(rating);
        //commentEditText.setText("별점 : " + rating);
    }

    //책 추가 Lottie애니메이션을 호출한다.
    private void showProgressDialog(){
        new LottieCompleteBookDialogFragment().newInstance().
                show(getSupportFragmentManager(),"");
    }

}

















