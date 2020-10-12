package com.example.dogzear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.dto.BookItem;
import com.example.dogzear.dto.MemoItem;
import com.example.dogzear.saveAndLoad.SaveAndLoad;
import com.example.dogzear.service.MusicService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private static final String TAG = "SettingActivity";
    private static final String SETTINGS_BOOK_JSON = "settings_book_json";//책 리스트를 담은 SharedPreference의 파일 이름
    private static final String SETTINGS_MEMO_JSON = "settings_memo_json";//메모 리스트를 담은 SharedPreference의 파일 이름
    private Button lockButton;//잠금화면 설정
    private Switch musicSwitch;//독서음악 설정
    private Switch flashSwitch;//독서등 설정 (플래쉬 기능)
    private Switch deleteSwitch;//초기화 설정
    //독서음악 관련 버튼
    private Button playBtn;
    private Button pauseBtn;
    private Button stopBtn;
    //초기화 관련 버튼
    private Button deleteBtn;
    private Button deleteCancelBtn;
    //초기화 진행과정 프로그래스바
    private ProgressBar deleteProgress;
    //초기화 진행과정 삭제하는 파일 이름을 표시해준다
    private TextView fileName;
    //초기화 관련 AsyncTask클래스
    private DeleteTask deleteTask;

    //초기화시 지워줄 삭제하는 파일 이름
    private String deleteItemTitle;
    //private BookItem item;
    private ArrayList<BookItem> listViewItemList;
    private ArrayList<MemoItem> listViewMemoList;

    private SharedPreferences sharedPreferences;
    private boolean playMusic;
    private boolean isPlaying;
    private boolean flash;
    //프로그래스 바의 퍼센트
    private int percentage;
    //책 리스트의 총 개수
    private int bookCntIndex;
    //private int recoverBookCntIndex; //스레드를 중지시키고 다시 시작할때 초기값으로 설정해줄 변수 (recover-를 붙인 변수들의 역할)
    //메모 리스트의 총 개수
    private int memoCntIndex;
    //private int recoverMemoCntIndex;
    //책과 메모의 총 개수
    private int allDataIndex;
    //private int recoverAllDataIndex;
    /*삭제할 파일의 개수가 5개 -> 프로그래스바가 한 작업당(파일 하나를 삭제할시) 20씩 차올라야 100이 되면 작업이 완료된다.
     *삭제할 파일의 개수가 20개 -> 프로그래스바가 한 작업당(파일 하나를 삭제할시) 5씩 차올라야 100이 되면 작업이 완료된다.
     * plusAmount = 100 / [데이터의 개수]
     */
    private int plusAmount;

    private CameraManager cameraManager;
    private String cameraId;
    private boolean flashOn;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_app_setting);
        setContentView(R.layout.activity_app_setting_normal);
        //후면 카메라 객체를 가져온다.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(getApplicationContext(), "There is no camera flash.\n The app will finish!", Toast.LENGTH_LONG).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
            return;
        }

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        lockButton = (Button)findViewById(R.id.lock_button);
        musicSwitch = (Switch)findViewById(R.id.music_switch);
        flashSwitch = (Switch)findViewById(R.id.flash_switch);
        deleteSwitch = (Switch)findViewById(R.id.data_delete);
        playBtn = (Button)findViewById(R.id.play_btn);
        pauseBtn = (Button)findViewById(R.id.pause_btn);
        stopBtn = (Button)findViewById(R.id.stop_btn);
        deleteBtn = (Button)findViewById(R.id.delete_all_btn);
        deleteCancelBtn = (Button)findViewById(R.id.delete_cancel_btn);
        deleteProgress = (ProgressBar)findViewById(R.id.delete_progress);
        fileName = (TextView)findViewById(R.id.file_name);

        lockButton.setOnClickListener(this);
        musicSwitch.setOnCheckedChangeListener(this);
        flashSwitch.setOnCheckedChangeListener(this);
        deleteSwitch.setOnCheckedChangeListener(this);

        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        deleteCancelBtn.setOnClickListener(this);

        //저장되어있는 책과 메모의 데이터를 담는다. (책 -> listViewItemList, 메모 -> listViewMemoList)
        listViewItemList = SaveAndLoad.getStringArrayPref(getApplicationContext(), SETTINGS_BOOK_JSON);
        listViewMemoList = getStringArrayPref(getApplicationContext(), SETTINGS_MEMO_JSON);
        //삭제할 책리스트의 마지막 인덱스
        bookCntIndex = listViewItemList.size();
        memoCntIndex = listViewMemoList.size();
        allDataIndex = bookCntIndex + memoCntIndex;
        //while문에서 allDataIndex-- 로 시작하므로 갯수를 조정해준다. -> 그대로 사용해준다.
        memoCntIndex--;

        //지울 책이 있을경우 (없는 경우는 devideByZero error가 발생한다.)
        if(allDataIndex != 0) {
            plusAmount = 100 / allDataIndex;
            //plusAmountLast = plusAmount + (100 % plusAmount);
        }
        //로그값 확인
        Log.d(TAG,"listViewItemList 개수 :                 " + bookCntIndex);
        Log.d(TAG,"listViewMemoList 개수 :                 " + memoCntIndex);
        Log.d(TAG, "총 데이터 개수 :               " + allDataIndex);

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        //음악 재생 스위치의 상태값
        playMusic = sharedPreferences.getBoolean("playMusic",false);
        //현재 음악을 재생하고 있는지를 나타내는 상태값
        isPlaying = sharedPreferences.getBoolean("isPlaying", false);

        flash = sharedPreferences.getBoolean("flash", false);

        if(playMusic) {
            musicSwitch.setChecked(true);
            if(isPlaying) {
                playBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.VISIBLE);
            } else {
                playBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.VISIBLE);
            }
        }

        if(flash) {
            flashSwitch.setChecked(true);
        } else {
            flashSwitch.setChecked(false);
        }

//        musicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked == true) {
//                   // playBtn.setVisibility(View.VISIBLE);
//                   // stopBtn.setVisibility(View.VISIBLE);
//                    Log.d(TAG, "음악버튼이 눌렸다.");
//                } else {
//                    //playBtn.setVisibility(View.INVISIBLE);
//                    //stopBtn.setVisibility(View.INVISIBLE);
//                }
//
//            }
//        });

    }

    //데이터 초기화 관련 업무 AsyncTask
    class DeleteTask extends AsyncTask<Integer, Integer, Integer> {

        //doInBackground 가 실행되기 전에 프로그레스바를 보여주는 등의 필요한 초기화 작업을 하는데 사용된다.
        @Override
        protected void onPreExecute() {
            percentage = 0;
            //프로그래스바의 상태를 0으로 초기화 시킨다.
            deleteProgress.setProgress(percentage);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            //isCancelled()=> Task가 취소되었을때 즉 cancel될 때까지 반복
            while (isCancelled() == false) {
                percentage += plusAmount;
                allDataIndex--;
                //
                if (bookCntIndex >= 0) {
                    bookCntIndex--;
                }else {
                    memoCntIndex--;
                }

                //위에 onCreate()에서 호출한 excute(100)의 100을 사용할려면 이런식으로 해줘도 같은 결과가 나온다.
                //밑 대신 이렇게해도됨 if (value >= values[0].intValue())
                if (percentage >= 100) {
                    break;
                } else {
                    //publishProgress()는 onProgressUpdate()를 호출하는 메소드(그래서 onProgressUpdate의 매개변수인 int즉 Integer값을 보냄)
                    //즉, 이 메소드를 통해 백그라운드 스레드작업을 실행하면서 중간중간  UI에 업데이트를 할 수 있다.

                    //백그라운드에서는 UI작업을 할 수 없기 때문에 사용
                    //percnetage : 프로그래스바에 표시할 %
                    //bookCntIndex : 지워야할 책의 개수
                    //memoCntIndex : 지워야할 메모의 개수 (책에도 메모가 있지만 책의 메모와 메모자체는 구분한다.)
                    //allDataIndex : 지워야할 전체 데이터수. -> 전체데이터수를 파악하기 위한 인덱스
                    publishProgress(percentage, bookCntIndex, memoCntIndex, allDataIndex);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {}
            }
            return percentage;
        }

        //UI작업 관련 작업 (백그라운드 실행중 이 메소드를 통해 UI작업을 할 수 있다)
        //publishProgress(value)의 value를 값으로 받는다.values는 배열이라 여러개 받기가능
        protected void onProgressUpdate(Integer ... values) {
            percentage = values[0].intValue();
            bookCntIndex = values[1].intValue();
            memoCntIndex = values[2].intValue();
            allDataIndex = values[3].intValue();
            //삭제할 인덱스의 값을 전달받는다.
            //책의 개수가 0이 될때까지, 전체데이터수가 줄어들어 메모의 수와 같아질때까지 -> 이부분은 논리보다는 주먹구구식으로 때려맞춘 면이 있다.
            if(bookCntIndex >= 0 && allDataIndex >= memoCntIndex) {
                //bookCntIndex = values[1].intValue();
                //지워야할 파일을 표시하기위해 책 제목을 가져온다.
                deleteItemTitle = listViewItemList.get(bookCntIndex).getTitle();
                listViewItemList.remove(bookCntIndex);
            } else {
                //지워야할 파일을 표시하기위해 메모 제목을 가져온다.
                deleteItemTitle = listViewMemoList.get(memoCntIndex).getTitle();
                listViewMemoList.remove(memoCntIndex);
            }
            //deleteProgress.setProgress(values[0].intValue());
            deleteProgress.setProgress(percentage);
            //지워야할 책 혹은 메모의 제목을 표시한다. -> 실제 파일을 삭제할때처럼 보여주기 위해.
            fileName.setText(deleteItemTitle + " 지우는중....");
        }

        //이 Task에서(즉 이 스레드에서) 수행되던 작업이 종료(완료)되었을 때 호출됨
        protected void onPostExecute(Integer result) {
            deleteProgress.setProgress(0);
            fileName.setText("완료되었습니다");

            SaveAndLoad.setStringArrayPref(getApplicationContext(),SETTINGS_BOOK_JSON, listViewItemList);
            setStringArrayPref( SETTINGS_MEMO_JSON, listViewMemoList);
            Log.d(TAG, "남은 책의 아이템 개수 : " + listViewItemList.size());
            Log.d(TAG, "남은 메모의 아이템 개수 : " + listViewMemoList.size());
            Log.d(TAG, "남은 데이터 (allDataIndex) : " + allDataIndex);
        }

        //Task가 취소되었을때 호출
        protected void onCancelled() {
            deleteProgress.setProgress(0);
            fileName.setText("취소되었습니다");
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(buttonView == musicSwitch) {
            //음악스위치가 켜졌을때
            if(musicSwitch.isChecked()) {
                playBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.VISIBLE);

                editor.putBoolean("playMusic", true);

            } else {
                //꺼졌을때
                stopService(new Intent(getApplicationContext(), MusicService.class));
                playBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.GONE);

                editor.putBoolean("playMusic", false);
            }
            Log.d(TAG, "음악버튼이 눌렸다.");
            showToast("음악버튼이 눌렸다.");
        }else if (buttonView == flashSwitch) {
            if(flashSwitch.isChecked()) {
                flashlight();
                editor.putBoolean("flash", true);
                Log.d(TAG, "플래쉬버튼이 눌렸다.");
                showToast("플래쉬버튼이 눌렸다.");
            } else {
                flashlight();
                editor.putBoolean("flash", false);
                Log.d(TAG, "플래쉬버튼이 꺼졌다.");
                showToast("플래쉬버튼이 꺼졌다.");
            }

            //삭제 스위치를 누르면 삭제 버튼을 visible로 나타낸다. (삭제는 리스키한 작업이므로 한번의 장치를 둔다.)
        } else if (buttonView == deleteSwitch) {
            //삭제스위치가 켜졌을때
            if(deleteSwitch.isChecked()) {
                deleteBtn.setVisibility(View.VISIBLE);
                //delteProgress.setVisibility(View.VISIBLE);
            }else {
                deleteBtn.setVisibility(View.GONE);
                deleteCancelBtn.setVisibility(View.GONE);
                deleteProgress.setVisibility(View.GONE);
                fileName.setVisibility(View.GONE);
            }
        }

        editor.commit();
    }

    @Override
    public void onClick(View view) {
        if(view == lockButton) {
            //잠금화면 설정은 코드의 복잡성을 고려해 다른 액티비티에서 설정하는 것으로 구현하였다.
            Intent intent = new Intent(getApplicationContext(), LockScreenActivity.class);
            startActivity(intent);
        } else if(view == playBtn) {
            Log.d(TAG, "재생버튼이 눌렸다.");
            //재생버튼을 누를시 일시정지 버튼도 보여준다.-> 재생을하지 않은채 일시정지를 하면 오류가 난다.
            pauseBtn.setVisibility(View.VISIBLE);
            Intent intent = new Intent(getApplicationContext(), MusicService.class);
            //intent.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("state", "start");

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //오레오 버젼 부터 백 그라운드 서비스에 대한 규정이 바뀌어서 백그라운드에서 1분정도만 작동한다.
                //그러므로 포그라운드로 작동해야한다.
                startForegroundService(intent);
            }else {
                startService(intent);
            }

        } else if (view == pauseBtn) {
            Intent intent = new Intent(getApplicationContext(), MusicService.class);
            intent.putExtra("state", "pause");
            startService(intent);

        } else if (view == stopBtn) {
            Log.d(TAG, "정지버튼이 눌렸다.");
            showToast("정지버튼이 눌렸다.");
            stopService(new Intent(getApplicationContext(), MusicService.class));
            //정지를 하면 Pause버튼을 가려준다.
            pauseBtn.setVisibility(View.GONE);
        } else if (view == deleteBtn) { //지우기 버튼을 눌렀을 경우

            if(allDataIndex <= 0) {
                fileName.setVisibility(View.VISIBLE);
                fileName.setText("지울 데이터가 없습니다.");
            } else {
                deleteProgress.setVisibility(View.VISIBLE);
                deleteCancelBtn.setVisibility(View.VISIBLE);
                fileName.setVisibility(View.VISIBLE);

                deleteTask = new DeleteTask();
                deleteTask.execute(bookCntIndex);
            }
        } else if (view == deleteCancelBtn) {//취소 버튼을 눌렀을 경우
            deleteTask.cancel(true);
            //기존에 삭제하고 있던 데이터들을 다시 초기의 값으로 복원시켜준다.
            //commit을 하기 전에 취소했으므로 ArrayList의 값만 줄어들었다.
            //줄어든 ArrayList의 값들을 삭제하기 전의 원 상태의 값으로 복원한다.
            listViewItemList = SaveAndLoad.getStringArrayPref(getApplicationContext(), SETTINGS_BOOK_JSON);
            listViewMemoList = getStringArrayPref(getApplicationContext(), SETTINGS_MEMO_JSON);
            bookCntIndex = listViewItemList.size();
            memoCntIndex = listViewMemoList.size();
            allDataIndex = bookCntIndex + memoCntIndex;
            memoCntIndex--;
            Log.d(TAG,"취소 눌렀을 때 =========== listViewItemList 개수 :                 " + bookCntIndex);
            Log.d(TAG,"취소 눌렀을 때 =========== listViewMemoList 개수 :                 " + memoCntIndex);
            Log.d(TAG, "취소 눌렀을 때 ===========  총 데이터 개수 :               " + allDataIndex);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void flashlight() {
        if (cameraId == null) {
            try {
                for (String id : cameraManager.getCameraIdList()) {
                    CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                    Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                    if (flashAvailable != null && flashAvailable
                            && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id;
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                cameraId = null;
                e.printStackTrace();
                return;
            }
        }

        flashOn = !flashOn;

        try {
            cameraManager.setTorchMode(cameraId, flashOn);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String message){
        Toast toast=Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    //SharedPreference에 저장되어있는 String으로 변환된 JSON 데이터를 가져와 ArrayList의 데이터에 담는다.
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


    //ArrayList의 데이터를 JSON타입으로 변환한후 SharedPreference에 저장하는 메소드
    private void setStringArrayPref(String key, ArrayList<MemoItem> items) {

        //첫번째 인자의 이름의 SharedPreference 파일을 만든다.
        SharedPreferences prefs = getSharedPreferences(SETTINGS_MEMO_JSON, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray jsonMemoArray = new JSONArray();
        JSONObject jsonMemoObject;

        for(int i = 0; i < items.size(); i++) {
            jsonMemoObject = new JSONObject();
            try {
                jsonMemoObject.put("title",items.get(i).getTitle());
                jsonMemoObject.put("content", items.get(i).getContent());
                jsonMemoObject.put("page", items.get(i).getPage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonMemoArray.put(jsonMemoObject);
        }

        if(!items.isEmpty()) {
            editor.putString(key, jsonMemoArray.toString());
        } else {
            editor.putString(key, null);
        }

        //commit() -> 데이터를 persistent storage에 저장하는 과정이 동기화되어 storage에 write한 결과를 사용자에게 boolean으로 반환한다.
        //apply() -> 내부적으로 비동기 방식으로 작동한다. 해당 값을 메모리에 캐시하고 background에서 스토리지에 데이터를 write하기 때문에 apply 호출 직후 다시
        //읽더라도 캐시에서 올바른 값을 읽어올 수 있다. SharedPreferences는 프로세스 내에서 싱글톤 방식의 인스턴스이기 때문에 만약 반호나 값을 필요로 하지 않는다면
        //commit()대신 apply()를 쓰는 것이 안전하다. -> 출처 : https://chuumong.tistory.com/entry/Sharedpreferenceseditor-Apply%EC%99%80-Commit%EC%9D%98-%EC%B0%A8%EC%9D%B4
        editor.apply();

    }

}
