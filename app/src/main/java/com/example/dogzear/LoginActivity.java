package com.example.dogzear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private InputMethodManager imm;
    private Button loginBtn;
    private Button signInBtn;
    private EditText editTextId;
    private EditText editTextPwd;
    private CheckBox loginCheck;
    private LinearLayout linearLayout;

    //사용자 정보
    SharedPreferences userInfo;
    String id = "";
    String pwd = "";
    //자동 로그인
    boolean autoLogin;
    //첫 방문시 추가정보를 얻기위한 액티비티를 띄어주기 위한 값
    boolean hasVisited;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Toast.makeText(this, "onCreate 호출됨", Toast.LENGTH_LONG);
        Log.d(TAG, "로그인화면으로 들어올때 onCreate()를 거친다?");

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        linearLayout = (LinearLayout)findViewById(R.id.login_container);
        loginBtn = (Button) findViewById(R.id.login_btn);
        signInBtn = (Button) findViewById(R.id.sign_in_btn);
        editTextId = (EditText) findViewById(R.id.et_id);
        editTextPwd = (EditText) findViewById(R.id.et_pwd);
        loginCheck = (CheckBox) findViewById(R.id.login_check);

        loginBtn.setOnClickListener(this);
        signInBtn.setOnClickListener(this);
        linearLayout.setOnClickListener(this);
        //loginCheck.setOnClickListener(this);

        userInfo = getSharedPreferences("user_info", MODE_PRIVATE);
        //자동로그인 여부 확인
        autoLogin = userInfo.getBoolean("auto", false);
        //방문한적 있는지 확인
        hasVisited = userInfo.getBoolean("hasVisited", false);
        //자동 로그인이 되어 있으면 바로 로그인한다.
        if(autoLogin) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "자동로그인 하셨습니다", Toast.LENGTH_SHORT).show();
        }

    }

    //같은화면을 다시한번 부를시 호출된다.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "로그인화면으로 들어올때 onNewIntent()를 거친다?");
    }

    @Override
    public void onClick(View view) {
        //SharedPreferences의 값을 불러온다.
        id = userInfo.getString("userId", "userId");
        pwd = userInfo.getString("userPwd", "userPwd");
        //사용자가 입력한 값을 불러온다.
        String userId = editTextId.getText().toString().trim();
        String userPwd = editTextPwd.getText().toString().trim();
        Log.d(TAG, "ID : " + userId +" , PWD : " + userPwd);

        imm.hideSoftInputFromWindow(editTextId.getWindowToken(),0);
        Log.d(TAG, "터치를 한다.");
        imm.hideSoftInputFromWindow(editTextPwd.getWindowToken(),0);

        if(view == loginBtn) {

            if(id.equals(userId) && pwd.equals(userPwd)) {
                SharedPreferences.Editor editor = userInfo.edit();
                //첫 방문시
                if(!hasVisited) {
                    //자동로그인 체크가 되어있는경우
                    if (loginCheck.isChecked()) {
                        editor.putBoolean("auto", true);
                       // editor.commit();
                    } else {
                        editor.putBoolean("auto", false);
                       // editor.commit();
                    }
                    editor.putBoolean("hasVisited", true);
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), FirstVisitActivity.class);
                    startActivity(intent);
                    Toast.makeText(this, "첫 방문 짝짝짝", Toast.LENGTH_SHORT).show();
                } else {
                    //첫 방문이 아닌경우
                    if (loginCheck.isChecked()) {
                        editor.putBoolean("auto", true);
                        // editor.commit();
                    } else {
                        editor.putBoolean("auto", false);
                        // editor.commit();
                    }
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "아이디와 비밀번호를 제대로 입력하세요", Toast.LENGTH_SHORT).show();
                editTextId.getText().clear();
                editTextPwd.getText().clear();
            }
        } else if(view == signInBtn) {
            Toast.makeText(this, "Signin버튼 누름", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
        }
    }


}
