package com.example.dogzear;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogzear.game.MolegameActivity;
import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.mediarecorder.AudioRecorderActivity;
import com.example.dogzear.openapi.DustOpenAPIActivity;
import com.example.dogzear.saveAndLoad.SaveAndLoad;
import com.example.dogzear.service.MusicService;
import com.google.android.material.navigation.NavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "MainActivity";

    private ImageView logoImg;
    private Button profileBtn;
    private Button bookletBtn;
    private Button calendarBtn;
    private Button chartBtn;
    private Button cameraBtn;
    private CircleImageView profileImg;
    private TextView textViewId;
    private TextView profileID;
    private TextView profileText;
    private NavigationView navigationView;
    private View headerView;

    Animation rotateAnimation;

    private DrawerLayout drawerLayout;
   // private Context context = this;

    //값이 null일경우 setText의 경우 오류가 난다.
    String userId = "";
    String userPwd = "";
    String userText = "";//프로필 문구
    // back키를 두번 이사 ㅇ연속해서 누를 때만 액티비티 종료
    private boolean isBackPressedOnce = false;
    private boolean isTouchForEasterEgg = false;

    //사용자의 ID와 PW를 저장한 파일
    SharedPreferences userInfo;
    //사용자의 세부정보(나이, 성별, 장르)을 저장한 파일
    SharedPreferences userInfoDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //앱을 처음 시작할때 권한요청을 한다.
        TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("거부하셨습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

        userInfo = getSharedPreferences("user_info", MODE_PRIVATE);
        userId = userInfo.getString("userId","user");
        userPwd = userInfo.getString("userPwd", "pwd");
        userText = userInfo.getString("userText", "프로필 문구를 입력하세요");
//        Intent intent = getIntent();
//        userId = intent.getStringExtra("userId");
//        userPwd = intent.getStringExtra("userPwd");
//        Log.d(TAG, "ID : " + userId +" , PWD : " + userPwd);
//        textViewId = findViewById(R.id.tv_id);////////////////////////
//        profileBtn = (Button)findViewById(R.id.profile_btn);////////////////////////

//        textViewId.setText(userId + " 님");////////////////////////
//        profileBtn = (Button) findViewById(R.id.profile_btn);////////////////////////
        bookletBtn = (Button) findViewById(R.id.booklet_btn);
        calendarBtn = (Button) findViewById(R.id.calendar_btn);
        chartBtn = (Button) findViewById(R.id.chart_btn);
        cameraBtn = (Button) findViewById(R.id.camera_btn);
        logoImg = (ImageView)findViewById(R.id.toolbar_title);

        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.logo_rotate);
        logoImg.startAnimation(rotateAnimation);
        //네비게이션드로어를 위해 툴바를 장착한다.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        //기존 타이틀 지우기
        actionbar.setDisplayShowTitleEnabled(false);
        //Toolbar왼쪽에 버튼이 생성된다. -> layout에서 생성하는게 아니다 . 주의할 것.
        actionbar.setDisplayHomeAsUpEnabled(true);
        //버튼의 이미지를 설정한다.// 메뉴버튼 -> material_design을 이용함
        actionbar.setHomeAsUpIndicator(R.drawable.ic_stat_name);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
       // navigationMenuView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //네비게이션의 헤더에 정보를 표시해주기 위해 얻어온다.
        headerView = navigationView.getHeaderView(0);
        //네비게이션에 표시할 아이디, 프로필 문구, 프로필사진
        profileID = (TextView)headerView.findViewById(R.id.profile_id);
        profileText = (TextView)headerView.findViewById(R.id.profile_text);
        profileImg = (CircleImageView)headerView.findViewById(R.id.profile_img);
        //profile_text
        //아이디와 프로필구문 표시
        //Toast.makeText(this.getApplicationContext(), profileID.getText().toString() , Toast.LENGTH_SHORT).show();
        profileID.setText(userId + " 님 안녕하세요");
        profileText.setText(userText);

        String profileImageStr = userInfo.getString("user_profile_img", "");
        if(!profileImageStr.equals("")) {
            Bitmap bitmap = SaveAndLoad.StringToBitMap(profileImageStr);
            profileImg.setImageBitmap(bitmap);
        }

//        profileBtn.setOnClickListener(this);////////////////////////
        logoImg.setOnClickListener(this);
        bookletBtn.setOnClickListener(this);
        calendarBtn.setOnClickListener(this);
        chartBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);

        /*
        하나의 키에 담은 값들을 꺼내온다.

         */
//        userInfoDetail = getSharedPreferences("user_info_detail",MODE_PRIVATE);
//        Set<String> infos = userInfoDetail.getStringSet("infos", null);
//        Log.d(TAG, "값은 총 " + infos.size() + " 개 들어있다. ");
//
//        HashMap<String,String> convertInfos = new HashMap<>();
//
//
//        int i = 0;
//        Iterator<String> iterator = infos.iterator();
//        while(iterator.hasNext()) {
//            String element = iterator.next();
//            String[] splitElement = element.split(":");
//            convertInfos.put(splitElement[0],splitElement[1]);
//            i++;
//            Log.d(TAG, i + " 번재 값 : " + splitElement[0] + " " + splitElement[1]);
//        }
//
//        Log.d(TAG, "hashmap에서 가져온 값   :    " + convertInfos.get("genre"));

    }


    int easterEggCnt = 0;

    @Override
    public void onClick(View view) {

        if(view == profileBtn) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userID", userId);
            intent.putExtra("userPwd", userPwd);
            startActivity(intent);
        }else if(view == bookletBtn) {
            /*책갈피 화면을 클릭하면 라이브러리 화면으로 이동한다.*/
            Intent intent = new Intent(this, LibraryActivity.class);
            intent.putExtra("userID", userId);
            startActivity(intent);
        }else if(view == calendarBtn) {
            Toast.makeText(this, "캘린더를 클릭했습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, BookCalendarActivity.class);
            startActivity(intent);
        }else if(view == chartBtn) {
            Toast.makeText(this, "차트를 클릭했습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, BookCompleteActivity.class);
            startActivity(intent);
        }else if(view == cameraBtn){
            Toast.makeText(this, "메모하기를 클릭했습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MemoBookActivity.class);
            intent.putExtra("userID", userId);
            intent.putExtra("userPwd", userPwd);
            startActivity(intent);
        }else if(view == logoImg) {
            //이미지 로고를 2초내에 6번 터치를 해야 화면이 바뀐다.
            Handler easterHandler = new Handler();
            if(isTouchForEasterEgg) {
                Toast.makeText(this, "어떻게 알고 들어온거지???", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MolegameActivity.class);
                startActivity(intent);
            } else {
                easterEggCnt++;
                switch(easterEggCnt) {
                    case 1:
                        easterHandler.postDelayed(EasterEggTask, 2000);
                        break;
                    case 2:
                        Toast.makeText(this, "누르지마시오.....", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(this, "??? 정말 누르지 말라니깐?", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        isTouchForEasterEgg = true;
                        break;
                }
            }
        }

    }

    //1초 이내로 백버튼을 한번 더 누르지 않을시 isBackPressedOnce를 false로 바꿔 앱 종료를 막는다.
    private final Runnable EasterEggTask = new Runnable() {
        @Override
        public void run() {
            easterEggCnt = 0;
            if(!isTouchForEasterEgg) {
                Toast.makeText(getApplicationContext(), "초기화 됐지롱 낄낄..", Toast.LENGTH_SHORT).show();
            }
            isTouchForEasterEgg = false;
        }
    };

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "액티비티가 다시 시작되면 여기를 거친다." );
    }

    //Activity가 destory 되는 상황을 파악하기 위해
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "액티비티가 종료되기 전에 onSaveInstanceState가 실행된다." );
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy가 실행되었다. MainActivity에서");
    }

    @Override
    protected void onResume() {
        //프로필을 변경후 바로 적용해주기 위한 코드 -> onCreate()의 코드와 동일하다.
        userId = userInfo.getString("userId","user");
        userPwd = userInfo.getString("userPwd", "pwd");
        userText = userInfo.getString("userText", "프로필 문구를 입력하세요");
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // navigationMenuView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //네비게이션의 헤더에 정보를 표시해주기 위해 얻어온다.
        headerView = navigationView.getHeaderView(0);
        profileID = (TextView)headerView.findViewById(R.id.profile_id);
        profileText = (TextView)headerView.findViewById(R.id.profile_text);
        profileImg = (CircleImageView)headerView.findViewById(R.id.profile_img);
        //profile_text
        //아이디와 프로필구문 표시
        //Toast.makeText(this.getApplicationContext(), profileID.getText().toString() , Toast.LENGTH_SHORT).show();
        profileID.setText(userId + " 님 안녕하세요");
        profileText.setText(userText);

        String profileImageStr = userInfo.getString("user_profile_img", "");
        if(!profileImageStr.equals("")) {
            Bitmap bitmap = SaveAndLoad.StringToBitMap(profileImageStr);
            profileImg.setImageBitmap(bitmap);
        }
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        Handler timerHandler = new Handler();
        /*Back버튼의 기능을 drawerNavigation이 열렸을때 닫는 기능만 구현한다.
         기존의 Back버튼 기능인 전 액티비티로 돌아가는 건 막는다. 로그인 페이지로 돌아가지 못하게 하기 위해.*/
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if(isBackPressedOnce) {
//            moveTaskToBack(true);						// 태스크를 백그라운드로 이동
//            finishAndRemoveTask();						// 액티비티 종료 + 태스크 리스트에서 지우기
//            android.os.Process.killProcess(android.os.Process.myPid());	// 앱 프로세스 종료
            ActivityCompat.finishAffinity(this);//액티비티를 종료
            System.exit(0);//프로세스 종료
        } else {
            Toast.makeText(this, "두번연속으로 누를시 종료됩니다.", Toast.LENGTH_SHORT).show();
            isBackPressedOnce = true;
            timerHandler.postDelayed(timerTask, 1000);
        }
    }

    //1초 이내로 백버튼을 한번 더 누르지 않을시 isBackPressedOnce를 false로 바꿔 앱 종료를 막는다.
    private final Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            isBackPressedOnce = false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            //toolbar의 home버튼을 눌렀을때 동작
            case android.R.id.home: {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //네비게이션 메뉴 선택 이벤트
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();

        int id = menuItem.getItemId();
        String title = menuItem.getTitle().toString();

        if(id == R.id.account){
            Toast.makeText(this, title + ": 계정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.setting){
            Toast.makeText(this, title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.record) {
            Toast.makeText(this, title + ": 좋은 문장을 목소리로 담아보아요.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AudioRecorderActivity.class);
            startActivity(intent);
        } else if (id == R.id.map) {
            //카카오 지도로 이동한다.
            Toast.makeText(this, title + ": 지도 페이지로 이동", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);

        } else if (id == R.id.climate) {
            Toast.makeText(this, title + ": 대기오염 페이지로 이동", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DustOpenAPIActivity.class);
            startActivity(intent);

        } else if (id == R.id.logout) {
            Toast.makeText(this, title + ": 로그아웃 시도중", Toast.LENGTH_SHORT).show();
            //Todo 노래가 실행중일땐 로그아웃하면 실행중인 음악을 끈다.
            stopService(new Intent(getApplicationContext(), MusicService.class));
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return true;
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(getApplicationContext(), "권한이 허용됨",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한이 거부됨",Toast.LENGTH_SHORT).show();
        }
    };
}
