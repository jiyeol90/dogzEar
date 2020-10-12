package com.example.dogzear;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogzear.adapter.MemoAdatper;
import com.example.dogzear.dto.MemoItem;
import com.example.dogzear.lock.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MemoBookActivity extends BaseActivity {
    private static final String TAG = "MemoBookActivity";

    private ArrayList<MemoItem>  RecyclerViewItemList;
    private MemoAdatper adapter;
    private int count = -1;

    //ArrayList를 Json으로 변환하고 저장할 SharedPreference의 파일 이름
    private static final String SETTINGS_MEMO_JSON = "settings_memo_json";// ->settings_memo_json 으로 수정할것.


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_book);

        //리사이클러뷰 생성 -> 레이아웃매니저 적용
        RecyclerView recyclerview = findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(linearLayoutManager);

        RecyclerViewItemList = new ArrayList<MemoItem>();
        //SharedPreference를 불러온다.
        RecyclerViewItemList = getStringArrayPref(getApplicationContext(), SETTINGS_MEMO_JSON);

        adapter = new MemoAdatper(this,RecyclerViewItemList);
        recyclerview.setAdapter(adapter);

        /*샘플 아이템 넣어주기*/
        SharedPreferences pref = getSharedPreferences("user_info", MODE_PRIVATE);
        boolean inputItems  = pref.getBoolean("isInputMemo",false);
        if(!inputItems) {
            adapter.addItem("지지 않는다는 말", "\"아무도 이기지 않았건만, 나는 누구에게도지지 않았다.\"", "56p");
            adapter.addItem("카스테라", "\"소설을 이해하려 , 해석하려고 고민한다\"", "126p");
            adapter.addItem("검은 꽃", "\"아무도 없는 곳이었기에 나는 서있을수 있었다.\"", "56p");
            adapter.addItem("농담", "\"우리의 삶의 모든 중대한 순간들은 단 한번뿐\"", "36p");
            adapter.addItem("달과 6펜스", "\"어떤 사람들은 자기가 태어날 곳이 아닌 데서 태어나기도 한다.\"", "116p");
            inputItems = true;
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isInputMemo", inputItems);
            editor.apply();
        }
//        adapter.addItem("지지 않는다는 말", "\"아무도 이기지 않았건만, 나는 누구에게도지지 않았다.\"", "56p");
//        adapter.addItem("카스테라", "\"소설을 이해하려 , 해석하려고 고민한다\"", "126p");
//        adapter.addItem("검은 꽃", "\"아무도 없는 곳이었기에 나는 서있을수 있었다.\"", "56p");
//        adapter.addItem("농담", "\"우리의 삶의 모든 중대한 순간들은 단 한번뿐\"", "36p");
//        adapter.addItem("달과 6펜스", "\"어떤 사람들은 자기가 태어날 곳이 아닌 데서 태어나기도 한다.\"", "116p");
//        adapter.addItem("지지 않는다는 말", "\"아무도 이기지 않았건만, 나는 누구에게도지지 않았다.\"", "56p");
//        adapter.addItem("카스테라", "\"소설을 이해하려 , 해석하려고 고민한다\"", "126p");
//        adapter.addItem("검은 꽃", "\"아무도 없는 곳이었기에 나는 서있을수 있었다.\"", "56p");
//        adapter.addItem("농담", "\"우리의 삶의 모든 중대한 순간들은 단 한번뿐\"", "36p");
//        adapter.addItem("달과 6펜스", "\"어떤 사람들은 자기가 태어날 곳이 아닌 데서 태어나기도 한다.\"", "116p");

        /*리사이클러뷰의 아이템의 경계선 그려주기*/
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerview.getContext(),
                linearLayoutManager.getOrientation());

        recyclerview.addItemDecoration(dividerItemDecoration);

        recyclerview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerview, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                MemoItem item = RecyclerViewItemList.get(position);
                Toast.makeText(getApplicationContext(), " < "+item.getTitle() + "  > \n" + item.getContent() + " - " + item.getPage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        Button memo_btn = (Button)findViewById(R.id.book_memo_btn);

        memo_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MemoBookActivity.this);
                View view = LayoutInflater.from(MemoBookActivity.this).inflate(R.layout.edit_box, null, false);
                builder.setView(view);

                final Button submitBtn = (Button) view.findViewById(R.id.submit_btn);
                final EditText et_title = (EditText) view.findViewById(R.id.et_title);
                final EditText et_content = (EditText) view.findViewById(R.id.et_content);
                final EditText et_page = (EditText) view.findViewById(R.id.et_page);

                //데이터가 없는 경우에는 삽입을 하는 기능을 나타낸다.
                submitBtn.setText("삽입");

                final AlertDialog dialog = builder.create();

                submitBtn.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        String title = et_title.getText().toString();
                        String content = et_content.getText().toString();
                        content = "\"" + content + "\"";
                        String page = et_page.getText().toString();

                        MemoItem item = new MemoItem(title, content, page);
                        RecyclerViewItemList.add(0, item);

                        adapter.notifyItemInserted(0);

                        dialog.dismiss();
                    }
                });

                dialog.show();

//                count++;
//
//                RecyclerViewItem data = new RecyclerViewItem(count +"카스테라", "너무나 절망적인", "45p");
//                RecyclerViewItemList.add(data);
//                adapter.notifyDataSetChanged();
            }
        });

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    /*ListView의 setOnItemClickListener와 달리 아이템의 position을 바로 알려주지 않는다.직접 이벤트 함수 내부에서 position을 계산해야 한다.*/
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MemoBookActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MemoBookActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){

                /*사용자가 한 번 터치했다는 것을 감지해 true가 반환되면 이벤트가 실행.*/
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                                            /*지정된 점 위의 view를 찾아준다.*/
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if(child != null && clickListener != null) {
                                                                    /*어댑터에서 지정된 view에 해당하는 위치를 반환한다.*/
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if(child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
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

    //화면전환 혹은 앱 전환, 종료시 항상 거쳐가는 생명주기 이므로 이곳에 리사이클러뷰의 데이터를 저장한다.
    @Override
    protected void onPause() {
        super.onPause();

        setStringArrayPref( SETTINGS_MEMO_JSON, RecyclerViewItemList);
        Log.d(TAG, "Put JSON");
    }
}


















