package com.example.dogzear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;



import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private InputMethodManager imm;
    private Button loginBtn;
    private Button signInBtn;
    private Button btn_custom_login;
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
    /*
     * 카카오 로그인 관련 코드는
     * KAKAO_LOGIN 표시를 해둔다.
     */
    private SessionCallback sessionCallback = new SessionCallback();
    Session session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Toast.makeText(this, "onCreate 호출됨", Toast.LENGTH_LONG);
        Log.d(TAG, "로그인화면으로 들어올때 onCreate()를 거친다?");

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        linearLayout = (LinearLayout) findViewById(R.id.login_container);
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
        if (autoLogin) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "자동로그인 하셨습니다", Toast.LENGTH_SHORT).show();
        }

        //KAKAO_LOGIN
        btn_custom_login = (Button) findViewById(R.id.btn_custom_login);

        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);

        btn_custom_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);

            }
        });

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
        Log.d(TAG, "ID : " + userId + " , PWD : " + userPwd);

        imm.hideSoftInputFromWindow(editTextId.getWindowToken(), 0);
        Log.d(TAG, "터치를 한다.");
        imm.hideSoftInputFromWindow(editTextPwd.getWindowToken(), 0);

        if (view == loginBtn) {

            if (id.equals(userId) && pwd.equals(userPwd)) {
                SharedPreferences.Editor editor = userInfo.edit();
                //첫 방문시
                if (!hasVisited) {
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
        } else if (view == signInBtn) {
            Toast.makeText(this, "Signin버튼 누름", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
        Log.d(TAG, "세션 콜백이 삭제되었습니다.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //////////////////////////////////////////////////
    public class SessionCallback implements ISessionCallback {

        // 로그인에 성공한 상태
        @Override
        public void onSessionOpened() {
            requestMe();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        // 로그인에 실패한 상태
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }

        // 사용자 정보 요청
        public void requestMe() {
            UserManagement.getInstance()
                    .me(new MeV2ResponseCallback() {
                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                        }

                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                        }

                        @Override
                        public void onSuccess(MeV2Response result) {
                            Log.i("KAKAO_API", "사용자 아이디: " + result.getId());

                            UserAccount kakaoAccount = result.getKakaoAccount();
                            if (kakaoAccount != null) {

                                // 이메일
                                String email = kakaoAccount.getEmail();

                                if (email != null) {
                                    Log.i("KAKAO_API", "email: " + email);

                                } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                                    // 동의 요청 후 이메일 획득 가능
                                    // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.

                                } else {
                                    // 이메일 획득 불가
                                }

                                // 프로필
                                Profile profile = kakaoAccount.getProfile();

                                if (profile != null) {
                                    Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                                    Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                                    Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());

                                } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                    // 동의 요청 후 프로필 정보 획득 가능

                                } else {
                                    // 프로필 획득 불가
                                }

                            }

                        }
                    });
        }

    }
}


