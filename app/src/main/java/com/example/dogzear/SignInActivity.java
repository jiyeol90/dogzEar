package com.example.dogzear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher{
    //Logcat을 찍기 위한 TAG
    private static final String TAG = "SingInActivity";

    private InputMethodManager imm;
    private Button duplicationBtn;
    private Button singInBtn;
    private EditText editTextID;
    private EditText editTextPW;
    private EditText editTextPWCheck;
    private TextView textViewAlarm;
    private RelativeLayout relativeLayout;

    //ID 중복체크 여부
    boolean duplicateCheck = false;
    //SharedPreferences에 저장할 사용자 id, password
    String id = "";
    String pwd = "";
    //SharedPreference파일이름, 앱내에서만 사용하능한 모드//
    SharedPreferences userInfo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        duplicationBtn = (Button) findViewById(R.id.check_duplication);
        singInBtn = (Button) findViewById(R.id.singIn);
        editTextID = (EditText) findViewById(R.id.et_id);
        editTextPW = (EditText) findViewById(R.id.et_pwd);
        editTextPWCheck = (EditText) findViewById(R.id.et_pwd_check);
        textViewAlarm = (TextView) findViewById(R.id.alarmMsg);
        relativeLayout = (RelativeLayout) findViewById(R.id.signing_container);

        duplicationBtn.setOnClickListener(this);
        singInBtn.setOnClickListener(this);
        relativeLayout.setOnClickListener(this);
        editTextPWCheck.addTextChangedListener(this);

        userInfo = getSharedPreferences("user_info",MODE_PRIVATE);
        //SharedPreference값 가져와서 확인하기
//        String value1 = userInfo.getString("userId", "아이디가 없다");
//        String value2 = userInfo.getString("userPwd", "패스워드가 없다");

//        Log.d(TAG, "ID : " + value1 + "PWD : " + value2);
        //SharedPreferences 값 확인하기
        String dataList = "";
        Map<String, ?> totalValue = userInfo.getAll();
        for(Map.Entry<String, ?> entry : totalValue.entrySet()) {
            dataList += entry.getKey().toString() + ": " + entry.getValue().toString() + "\r\n";
            Log.d("share : ", entry.getKey() + ": " + entry.getValue());
        }
    }

    @Override
    public void onClick(View view) {

        hideKeyboard();

        if(view == duplicationBtn) {

            // key, value(default) -> 값이 없을시 기본값을 대체한다.
            id = userInfo.getString("userId", "");
            String input_id = editTextID.getText().toString().trim();

            if(input_id.length() != 0) {
                //SharedPreferences에 ID값이 저장되어 있지 않은 경우
                if (id.length() == 0) {
                    id = input_id;
                    duplicateCheck = true;
                    Toast.makeText(this, "사용가능한 ID 입니다", Toast.LENGTH_SHORT).show();
                } else {//SharedPreferences에 ID값이 저장되어 있sms 경우
                    //SharedPreferences에 ID값이 저장되어 있는 경우 EditText에 입력한 값과 비교한다.
                    if (id.equals(input_id)) {
                        //Dialog를 띄어 중복되었다고 알려주고 EditText창을 초기화한다.
                        Toast.makeText(this, "ID를 사용할 수 없습니다", Toast.LENGTH_SHORT).show();
                        editTextID.getText().clear();
                        duplicateCheck = false; //제대로 중복체크를 통과하고 다시 다른 아이디를 입력할 경우
                    } else {
                        id = input_id;
                        duplicateCheck = true;
                        Toast.makeText(this, "사용가능한 ID 입니다", Toast.LENGTH_SHORT).show();
                    }
                }
            } else { //입력한 ID가 없을경우
                Toast.makeText(this, "ID를 입력하세요", Toast.LENGTH_SHORT).show();
            }
        } else if (view == singInBtn) {
            //비밀번호가 일치하고 중복체크를 한 경우에
            if(textViewAlarm.getText().toString().equals("일치합니다") && duplicateCheck) {
                pwd = editTextPWCheck.getText().toString().trim();
                //아이디와 패스워드를 저장한다.
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putString("userId", id);
                editor.putString("userPwd", pwd);
                editor.commit();
                Log.d(TAG, "ID : " + id + " PWD : " + pwd);
                Toast.makeText(this,"짝짝짝. 회원가입을 축하해요", Toast.LENGTH_SHORT).show();
                //회원가입 성공후 로그인 화면으로 돌아간다
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                if(!duplicateCheck) {
                    Toast.makeText(this, "중복체크를 확인하시오", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "제대로 입력하였는지 확인하시오", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    //텍스트가 변경될때마다 호출된다.
    @Override
    public void onTextChanged(CharSequence inputPwd, int start, int before, int count) {
        String originPwd = editTextPW.getText().toString();
        //첫번째 입력한 패스워드와 같을경우
        if(originPwd.equals(inputPwd.toString())) {
            textViewAlarm.setVisibility(View.VISIBLE);
            textViewAlarm.setTextColor(Color.BLUE);
            textViewAlarm.setText("일치합니다");
        } else {
            textViewAlarm.setVisibility(View.VISIBLE);
            textViewAlarm.setTextColor(Color.RED);
            textViewAlarm.setText("패스워드가 일치하지 않습니다");
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    //화면의 EditText 이외의 곳을 터치할시 키보드를 내린다. (백버튼으로만 키보드를 내리면 불편하기 때문에 적용.)
    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(editTextID.getWindowToken(),0);
        imm.hideSoftInputFromWindow(editTextPW.getWindowToken(),0);
        imm.hideSoftInputFromWindow(editTextPWCheck.getWindowToken(),0);
    }
}













