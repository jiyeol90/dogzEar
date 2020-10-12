package com.example.dogzear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogzear.dto.MemoItem;
import com.example.dogzear.lock.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private static final String SETTINGS_MEMO_JSON = "settings_memo_json";
    private ImageView book_logo;
    private TextView tv_logout;
    private TextView tv_profile;
    private TextView tv_random;
    private Animation rotateAnimation;

    //내가 저장한 메모들을 배너 광고 보여주듯이 뿌려줄 리스트
    ArrayList<MemoItem> banner = new ArrayList<MemoItem>();
    private Thread memoThread;


    //액티비티가 멈추면 스레드를 중지하기 위한 장치
    boolean threadStop = false;
    //메모 리스트의 메모개수
    private int bannerSize;
    //표시할 메모내용
    private String memo = "";
    //메모 리스트의 인덱스스
    private int memoIndex = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.logo_rotate);
        tv_logout = (TextView)findViewById(R.id.logout);
        tv_profile = (TextView)findViewById(R.id.change_profile);
        //메모장에 저장해 놓은 문구를 랜덤으로 일정한 시간간격으로 띄어주는 TextView
        tv_random = (TextView)findViewById(R.id.random_memo);
        book_logo = (ImageView)findViewById(R.id.profile);
        //flowAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.flow);
       // tv_random.startAnimation(flowAnimation);
        tv_logout.setOnClickListener(this);
        tv_profile.setOnClickListener(this);
        tv_random.setOnClickListener(this);

        book_logo.startAnimation(rotateAnimation);

        banner = getStringArrayPref(getApplicationContext(), SETTINGS_MEMO_JSON);
        //출력한 메모의 개수
        bannerSize = banner.size();

        //핸들러를 사용해 Runnable객체를 보내기위해서는 먼저 수신 스레드에서 핸들러 객체를 생성해야 한다.
        //핸들러 객체는 생성과 동시에 해당 스레드에서 동작 중인 루퍼와 메시지큐에 자동으로 연결
        final Handler handler = new Handler();
        //핸들러에 보낼 Runnable객체
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "표시되는 데이터 : " + memoIndex + ", 인덱스 값 : " + memoIndex);
                    memoIndex %= bannerSize;
                    //해당 인덱스의 메모객체에서 메모내용을 담는다.
                    memo = banner.get(memoIndex).getContent();
                    //액티비티의 TextView에 출력한다.
                    tv_random.setText(memo);
                    //인덱스값을 증가시킨다.
                    memoIndex++;

//                else {
//                    //인덱스 값이 총개수를 넘어가면 처음부터 다시 보여주기위해 0으로 초기화 한다.
//                    memoIndex = 0;
//                }
            }
        };

        //새로운 스레드 실행코드.
        memoThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(!threadStop) {
                        handler.post(runnable);
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            }
        });
        //threadStop = false;
        memoThread.start();

    }



    @Override
    public void onClick(View view) {
        //로그아웃
        if(view == tv_logout) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            /*기존의 아이디 화면을 출력할때 onCreate()로 생성하지않고 onNewIntent()를 거쳐 뿌려주자.
             액티비티 생성도 객체의 생성이므로 자원낭비에 유의할 것.*/
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //프로필 수정
        } else if(view == tv_profile){
            Intent intent = new Intent(getApplicationContext(), ProfileModifyActivity.class);
            startActivity(intent);
        }
    }

//    @Override
//    protected void onPause() {
//        //화면을 이동하거나 나갈때 스레드를 중지시킨다.
//        //onDestroy()에 작성하면 액티비티 종료시(백버튼, 앱종료)에만 실행되므로
//        // (화면이동시 고려)onPause()에 넣어준다.
//        threadStop = true;
//        super.onPause();
//    }

//    @Override
//    protected void onResume() {
//        //이동한 화면에서 백버튼으로 돌아왔을때 다시 메모 스레드를 실행한다.
//        super.onResume();
//        threadStop = false;
//        memoThread.start();
//        Log.d(TAG, "다시 돌아와서 스레드를 가동시킨다.");
//
//    }

        @Override
    protected void onDestroy() {
        threadStop = true;
        super.onDestroy();
    }

    private ArrayList getStringArrayPref(Context context, String key) {

        SharedPreferences prefs = getSharedPreferences(SETTINGS_MEMO_JSON, MODE_PRIVATE);
        String jsonMemoStr = prefs.getString(key, null);
        ArrayList<MemoItem> items = new ArrayList<MemoItem>();
        MemoItem item;

        if(jsonMemoStr != null) {
            try {
                //String으로 되어있는 JSONArray를 파싱한다.
                JSONArray jsonMemoArray = new JSONArray(jsonMemoStr);
                JSONObject jsonMemoObject;

                for(int i = 0; i < jsonMemoArray.length(); i++) {
                    jsonMemoObject = new JSONObject();
                    //JSONArray에서 인덱스 i 의 JSON 객체를 반환한다. -> 메모 객체의 배열이므로 배열의 요소값은 메모객체이다.
                    jsonMemoObject  = jsonMemoArray.getJSONObject(i);
                    String title = jsonMemoObject.optString("title");
                    String content = jsonMemoObject.optString("content");
                    String page = jsonMemoObject.optString("page");
                    item = new MemoItem(title, content, page);
                    //ArrayList에 메모장을 넣어준다.
                    items.add(item);
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return items;
    }

}
