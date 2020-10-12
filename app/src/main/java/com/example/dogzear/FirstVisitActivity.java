package com.example.dogzear;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FirstVisitActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener{

    private Button checkInfoBtn;
    private RadioGroup radioGroup;
    private Spinner ageSpinner;
    private Spinner genreSpinner;


    //SharedPreference 하나의 키에 여러값을 담을때 SET으로 담기때문에 순서를 지정해 주기 위해
    private final String GENDER = "gender";
    private final String AGE = "age";
    private final String GENRE = "genre";

    //성별, 나이, 선호장르 데이터 초기화
    private String gender = "";
    private String age = "";
    private String genre = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_visit);


        checkInfoBtn = (Button)findViewById(R.id.check_info);
        radioGroup = (RadioGroup)findViewById(R.id.gender_radio);
        ageSpinner = (Spinner)findViewById(R.id.age_spinner);
        genreSpinner = (Spinner)findViewById(R.id.genre_spinner);


        checkInfoBtn.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);


        //Spinner에 들어갈 item이 담긴 array 식별자 , android.R.layout.simple_spinner_dropdown_item는 기본으로 제공하는 형식(Spinner에 사용된 레이아웃)
        ArrayAdapter ageAdapter = ArrayAdapter.createFromResource(this, R.array.age, android.R.layout.simple_spinner_dropdown_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter genreAdapter = ArrayAdapter.createFromResource(this, R.array.genre, android.R.layout.simple_spinner_dropdown_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //age스피너를 어댑터에 연결해 준다.
        ageSpinner.setAdapter(ageAdapter);
        ageSpinner.setSelection(0,false);
        ageSpinner.setOnItemSelectedListener(this);
        //genre스피너를 어댑터에 연력해 준다.
        genreSpinner.setAdapter(genreAdapter);
        genreSpinner.setSelection(0,false);
        genreSpinner.setOnItemSelectedListener(this);

    }
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.male) {
                gender = "남성";
               // Toast.makeText(FirstVisitActivity.this, "당신의 성별은 남자 입니다.", Toast.LENGTH_SHORT).show();
            } else {
                gender = "여성";
                //Toast.makeText(FirstVisitActivity.this, "당신의 성별은 여자 입니다.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onClick(View view) {
        if(view == checkInfoBtn) {
            if(gender.length() == 0 || age.equals("나이") || genre.equals("장르")) {
                Toast.makeText(this, "정확히 선택하세요", Toast.LENGTH_SHORT).show();
            } else {
                //제대로 입력된 값을 Set<String> 에 담아 SharedPreferenced에 저장
                SharedPreferences sharedPreferences = getSharedPreferences("user_info_detail", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Set<String> infos = new HashSet<>();
                infos.add(GENDER + ":" + gender);
                infos.add(AGE + ":" + age);
                infos.add(GENRE + ":" + genre);
                editor.putStringSet("infos", infos);
                editor.commit();

                Toast.makeText(this, "성별 : " + gender + ", 나이 : " + age + " , genre : " + genre, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        //백버튼의 기능을 막는다. 무조건 선택을 강제하기 위해
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        String item = ageSpinner.getItemAtPosition(position).toString();
//        Toast.makeText(this, "ageSpinner 에서 선택한 아이템 : " + item, Toast.LENGTH_SHORT).show();
        //스피너를 등록하면 Oncreate에서 생성되자마자 이벤트를 감지하는 이슈가 있다. 해결을 못했지만 유의할것.
        if(parent == ageSpinner) {
            age = ageSpinner.getItemAtPosition(position).toString();
            //Toast.makeText(this, "ageSpinner 에서 선택한 아이템 : " + age, Toast.LENGTH_SHORT).show();
        }else if(parent == genreSpinner) {
            genre = genreSpinner.getItemAtPosition(position).toString();
            //Toast.makeText(this, "genreSpinner 에서 선택한 아이템 : " + genre, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
