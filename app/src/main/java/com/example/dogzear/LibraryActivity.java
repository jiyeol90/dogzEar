package com.example.dogzear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.dogzear.adapter.BookAdapter;
import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.saveAndLoad.SaveAndLoad;
import com.example.dogzear.dto.BookItem;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class LibraryActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "LibraryActivity";
    //SharedPreference의 파일 이름 설정.
    private static final String SETTINGS_BOOK_JSON = "settings_book_json";
    private static final String SETTING_COMPLETE_JSON = "setting_complete_json";
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final int REQUEST_BOOK_CONTENTS = 100;
    private final int REQUEST_MODIFY_CONTENTS = 200;

    private Button addBookBtn;
    private EditText bookSearch;
    private ImageView bookImage;
    private TextView tv_title;
    private TextView tv_author;
    private TextView tv_date;

    ListView listview;
    BookAdapter adapter;
    BookItem item;
    //내가 추가한 책의 리스트(완독하지 않음)
    ArrayList<BookItem> listViewItemList;
    //내가 완독한 책의 리스트
    ArrayList<BookItem> listViewComplete;
    //검색결과를 담을 리스트
    ArrayList<BookItem> filteredItemList;

    String bookTitle = "";
    String bookAuthor = "";
    String bookDate = "";
    String bookPage = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        //시작할때 SharedPreference에 저장된 String으로 저장된 JSON을 변환하여 ArrayList에 담는다.
        listViewItemList = SaveAndLoad.getStringArrayPref(getApplicationContext(), SETTINGS_BOOK_JSON);
        //완독한 책의 리스트
        listViewComplete = SaveAndLoad.getStringArrayPref(getApplicationContext(), SETTING_COMPLETE_JSON);

        for(int i = 0; i < listViewItemList.size(); i++) {
            item = listViewItemList.get(i);
            Log.d(TAG,"================================================================[ onCreate ]");
            Log.d(TAG,item.getTitle());
            Log.d(TAG,"메모리스트의 갯수 : " + item.getMemoList().size());
            for(int j = 0; j < item.getMemoList().size(); j++) {
                Log.d(TAG,i + "번째 책의 " + (j+1) + " 번째 메모" + item.getMemoList().get(j).getChapter());
            }
        }

        for(int i = 0; i < listViewComplete.size(); i++) {
            item = listViewComplete.get(i);
            Log.d(TAG,"==============================[[[완독]]]]============================[ onCreate ]");
            Log.d(TAG,item.getTitle());
            Log.d(TAG,"메모리스트의 갯수 : " + item.getMemoList().size());
            for(int j = 0; j < item.getMemoList().size(); j++) {
                Log.d(TAG,i + "번째 책의 " + (j+1) + " 번째 메모" + item.getMemoList().get(j).getChapter());
            }
        }

        //Adapter 생성
        adapter = new BookAdapter(listViewItemList);

        listview = (ListView) findViewById(R.id.book_list);
        listview.setAdapter(adapter);

        // 첫 번째 아이템 추가.  --> 세부정보에 사진이 안나오는 문제 해결할 것 (이미지를 직접 drawable파일에 넣을것.)
//        Bitmap bitmap = ((BitmapDrawable)ContextCompat.getDrawable(this, R.drawable.book_logo)).getBitmap();

        SharedPreferences pref = getSharedPreferences("user_info", MODE_PRIVATE);
        boolean inputItems = pref.getBoolean("isInputBook", false);
        if(!inputItems) {
            adapter.addItem(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.book_item_sample1)).getBitmap(),
                    "켄트 벡의 구현패턴", "켄트 백", "2020/09/08", "342");

            // 두 번째 아이템 추가.
            adapter.addItem(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.book_item_sample2)).getBitmap(),
                    "무지한 스승", "자크 랑시에르", "2020/09/10", "510");

            // 세번째 아이템 추가.
            adapter.addItem(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.book_item_sample3)).getBitmap(),
                    "카스테라", "박민규", "2020/09/10", "350");
            // 네번째 아이템 추가.
            adapter.addItem(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.book_item_sample4)).getBitmap(),
                    "공부란 무엇인가", "김영민", "2020/09/28", "450");
            // 다섯번째 아이템 추가.
            adapter.addItem(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.book_item_sample5)).getBitmap(),
                    "오리진", "루이스 디트넬", "2020/09/29", "350");
            // 여섯번째 아이템 추가.
            adapter.addItem(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.book_item_sample6)).getBitmap(),
                    "프리즘", "손원평", "2020/09/30", "280");
            inputItems = true;
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isInputBook", inputItems);
            //처음시작할때 아이템을 추가해
            SaveAndLoad.setStringArrayPref(this,SETTINGS_BOOK_JSON, adapter.getListViewItemList());
            //editor.apply();
            editor.commit();
        }

        bookSearch = (EditText) findViewById(R.id.search_bar);
        addBookBtn = (Button) findViewById(R.id.add_book_btn);
        bookImage = (ImageView) findViewById(R.id.book_image);
        tv_title = (TextView) findViewById(R.id.book_title);
        tv_author = (TextView) findViewById(R.id.book_author);
        tv_date = (TextView) findViewById(R.id.book_date);

        addBookBtn.setOnClickListener(this);

        //위에서 생성한 listview에 클릭 이벤트 핸들러 정의.
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();

                Toast.makeText(getApplicationContext(),  (position + 1) + " 번째 아이템을 터치하였습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
                /*수정해야 할 인덱스를 담는다.*/

                //adapter에 뿌려진 리스트는 FilteredList라는걸 명심할 것. position만으로는 처리를 하지 못한다.
                //왜냐하면 같은 1번째 아이템이어도 (index : 0 ) 기존의 원래데이터 리스트의 1번째 아이템과 FilteredList의 1번재 아이템은
                //다르기때문에 고유값 bookId으로 관리해야 한다.
                item = adapter.getItem(position);
                //item(책)의 bookId(고유값)를 담는다.
                String bookId = item.getBookId();

                //listViewItemList
                Log.d(TAG, "내가 누른 아이템  : " + item.getTitle());

                Log.d(TAG, "내가 누른 아이템 의 메모 갯수 : " + item.getMemoList().size());

                filteredItemList = adapter.getListViewItemList();

                //intent.putExtra("item", item);
                //선택한 아이템의 index-> filter를 했을때와 안했을때의 값의 배열이 다르기 때문에 쓰일수가 없다.
                intent.putExtra("position", position);
                intent.putExtra("bookId", bookId);

                startActivityForResult(intent,REQUEST_MODIFY_CONTENTS);

            }
        });


        bookSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String filterText = bookSearch.getText().toString();
                //EidtText에 입력한 필터링 텍스트가 별도의 팝업윈도우 형태로 출력되는것을 막기위해
                // ListView를 통하지 않고 Adapter로부터 직접 Filter 객체의 참조를 가져와서 filter() 함수를 호출
                ((BookAdapter)listview.getAdapter()).getFilter().filter(filterText);
            }
        });
    }

    // 검색을 수행하는 메소드
//    public void search(String charText) {
//
//        // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
//        searchItemList.clear();
//        // 문자 입력이 없을때는 모든 데이터를 보여준다.
//        if (charText.length() == 0) {
//            searchItemList.addAll(listViewItemList);
//        }
//        // 문자 입력을 할때..
//        else
//        {
//            // 리스트의 모든 데이터를 검색한다.
//            for(int i = 0;i < listViewItemList.size(); i++)
//            {
//                // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
//                if (listViewItemList.get(i).getTitle().toLowerCase().contains(charText))
//                {
//                    // 검색된 데이터를 리스트에 추가한다.
//                    searchItemList.add(listViewItemList.get(i));
//                }
//            }
//        }
//        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
//        adapter.setListViewItemList(searchItemList);
//        listview.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//    }

    /*임시저장할 값이 없을때, 저장완료나 취소시 초기화 해준다.*/
    //Intent tempData = null;


    @Override
    public void onClick(View view) {
        if(view == addBookBtn) {
            //수정하고 난 후의 결과값을 초기화 한다.
//            bookTitle = "";
//            bookAuthor = "";
//            bookDate = "";
            Intent intent = new Intent(this, AddBookActivity.class);

            Toast.makeText(getApplicationContext(), "입력할 bookTitle :  " + bookTitle, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "입력할 bookTitle. : " + bookTitle + ", bookAuthor : " + bookAuthor + ", bookDate : " + bookDate);
            startActivityForResult(intent,REQUEST_BOOK_CONTENTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "onActivityResult결과를 받아온다. : " + data.getStringExtra("back_button"));
        bookTitle = "";
        bookAuthor = "";
        bookDate = "";
        bookPage = "";
        //데이터에 대한 작업후 돌아왔을때 검색어에 입력했던 데이터를 없애준다.
        bookSearch.setText("");

        if (requestCode == REQUEST_BOOK_CONTENTS && resultCode == RESULT_OK) {

            /*등록버튼의 결과*/
            if(data.getStringExtra("RESULT").toString().equals("ADD")) {

                bookTitle = data.getStringExtra("title").toString();
                bookAuthor = data.getStringExtra("author").toString();
                bookDate = data.getStringExtra("startDate").toString();
                bookPage = data.getStringExtra("page").toString();

                String result = data.getStringExtra("RESULT");
                Log.d(TAG, "등록 버튼을 누르고 이곳으로 들어왔다.");

                //사진을 등록하지 않을시 기본 이미지로 대체한다.-> 기본이미지 사이즈 축소
                BitmapFactory.Options options = new BitmapFactory.Options();
                // inJustDecodeBounds = true일때 BitmapFactory.decodeResource는 리턴하지 않는다.
                // 즉 bitmap은 반환하지않고, options 변수에만 값이 대입된다.
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(getResources(),  R.drawable.book_logo, options);
                // 이미지 사이즈를 필요한 사이즈로 적당히 줄이기위해 계산한 값을
                // options.inSampleSize 에 2의 배수의 값으로 넣어준다.
                options.inSampleSize  = setSimpleSize(options, 100, 100);

                // options.inJustDecodeBounds 에 false 로 다시 설정해서 BitmapFactory.decodeResource의 Bitmap을 리턴받을 수 있게한다.
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.book_logo, options);

                // Bitmap bitmap = ((BitmapDrawable)ContextCompat.getDrawable(this, R.drawable.book_logo)).getBitmap();
                //사진을 등록해 Extra에 데이터가 담겨있는경우는 btyeArray를 deCoding하여 Image에 담는다.
                if(data.hasExtra("image")) {
                    byte[] byteArray = data.getByteArrayExtra("image");

                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    //bookImage.setImageBitmap(bitmap);
                }

                Toast.makeText(getApplicationContext(), "result : " + result, Toast.LENGTH_SHORT).show();
                /*등록시켜줘야 할 부분*/


                adapter.addItem(bitmap, bookTitle, bookAuthor, bookDate, bookPage) ;

                SaveAndLoad.setStringArrayPref(this,SETTINGS_BOOK_JSON, adapter.getListViewItemList());
                //Listview 갱신
                adapter.notifyDataSetChanged();

                //리스트뷰에 갱신을 한 후 추가한 책이 정보를 알림으로 알려준다.

                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                //알림을 터치할 때 발생할 인텐트를 준비하여 시스템에 등록하면 시스템에서 실제 사용자의 터치 이벤트가 발생할 때 등록한 인텐트를 발생해 주는 구조
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.book_logo)) //BitMap 이미지 요구
                        .setContentTitle(adapter.getCount()+"번째 책을 추가 하였습니다.")
                        .setContentText("책 제목 : " + bookTitle)
                        // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                        //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                        .setAutoCancel(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    builder.setSmallIcon(R.drawable.book_logo); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
                    CharSequence channelName  = "노티페케이션 채널";
                    String description = "오레오 이상을 위한 것임";
                    int importance = NotificationManager.IMPORTANCE_HIGH;

                    NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
                    channel.setDescription(description);

                    // 노티피케이션 채널을 시스템에 등록
                    assert notificationManager != null;
                    notificationManager.createNotificationChannel(channel);

                }else builder.setSmallIcon(R.drawable.book_logo); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

                assert notificationManager != null;
                notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴

                /*취소버튼의 결과*/
            } else if (data.getStringExtra("RESULT").toString().equals("CANCEL")) {
                String result = data.getStringExtra("RESULT");
                Log.d(TAG, "취소 버튼을 누르고 이곳으로 들어왔다.");
                Toast.makeText(getApplicationContext(), "result : "  + result, Toast.LENGTH_SHORT).show();

            }

        } else if(requestCode == REQUEST_MODIFY_CONTENTS && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", -1);

            //선택한 (position + 1)번째 책 삭제 기능
            if(data.getStringExtra("RESULT").toString().equals("DELETE")) {

                //넘겨받은 인덱스의 값을 지우고 JSON으로 저장한다.
                adapter.removeItem(position);
                Log.d(TAG, (position + 1) + "번째 책을 없앤다.");
                listViewItemList = adapter.getListViewItemList();
                Log.d(TAG, "남은 책 수" + listViewItemList.size());
                SaveAndLoad.setStringArrayPref(this,SETTINGS_BOOK_JSON, listViewItemList);

                //Listview 갱신
                adapter.notifyDataSetChanged();

                //선택한 (position + 1)번째 책 변경 기능
            } else if(data.getStringExtra("RESULT").toString().equals("MODIFY")) {
                Log.d(TAG, "변경할 인덱스값 : " + position + "========================================MODIFY");
                //해당 객체의 주소값 까지 담겨있으므로 item을 수정하면 기존의 ArrayList의 값까지 수정된다.
                item = adapter.getOriginItem(position);
                BookItem modifiedItem = data.getParcelableExtra("item");
                listViewItemList = adapter.getListViewItemList();
                item.setTitle(modifiedItem.getTitle());
                item.setAuthor(modifiedItem.getAuthor());
                item.setStartDate(modifiedItem.getStartDate());

                //메모 데이터는 계속 누적되서 더해지므로 초기화를 한번 해주고 더해준다.
                item.setMemoList(modifiedItem.getMemoList());
                //변경해준뒤 바로 적용하기 위해.
                listViewItemList = adapter.getListViewItemList();
                SaveAndLoad.setStringArrayPref(this,SETTINGS_BOOK_JSON, listViewItemList);
                adapter.notifyDataSetChanged();
                //선택한 (position + 1)번째 책 완독 기능
                //기존 책 리스트에서 제외시키고 (삭제)
                //완독 책 리스트에 넣어준다 (추가)
            } else if(data.getStringExtra("RESULT").toString().equals("COMPLETE")) {
/*
                resultIntent.putExtra("rating", bookRating);
                resultIntent.putExtra("comment", bookComment);
 */
                //완독한 해당 책을 얻는다.
                String rating = data.getStringExtra("rating");
                String comment = data.getStringExtra("comment");
                String finishDate = data.getStringExtra("finishDate");

                item = adapter.getOriginItem(position);
                item.setComment(comment);
                item.setRating(rating);
                item.setFinishDate(finishDate);

                Toast.makeText(this, (position + 1)+ " 번째 책 " + item.getTitle()+ "의 독서완료를 했습니다!!!!!!", Toast.LENGTH_SHORT).show();
                listViewComplete.add(item);
                listViewItemList.remove(position);

                SaveAndLoad.setStringArrayPref(getApplicationContext(),SETTING_COMPLETE_JSON, listViewComplete);
                SaveAndLoad.setStringArrayPref(getApplicationContext(),SETTINGS_BOOK_JSON, listViewItemList);

            }
        }
    }


    //BackButton으로 이동했을때의 생명주기. -> 성공할 시 저장할 것.
    @Override
    protected void onRestart() {
        super.onRestart();
        listViewItemList = SaveAndLoad.getStringArrayPref(this, SETTINGS_BOOK_JSON);

        adapter.setListViewItemList(listViewItemList);

        listview.setAdapter(adapter);

        for(int i = 0; i < listViewItemList.size(); i++) {
            item = listViewItemList.get(i);
            Log.d(TAG,"================================================================[ onRestart() ]");
            Log.d(TAG,(i+1) + "번째 책 : " +item.getTitle());
            Log.d(TAG,"메모리스트의 갯수 : " + item.getMemoList().size());
            for(int j = 0; j < item.getMemoList().size(); j++) {
                Log.d(TAG,i + "번째 책의 " + j + " 번째 메모(페이지) : " + item.getMemoList().get(j).getPage());
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //화면이동, 홈버튼으로 나가거나 앱 종료시 저장해준다.
        SaveAndLoad.setStringArrayPref(getApplicationContext(),SETTINGS_BOOK_JSON, adapter.getListViewItemList());
        Log.d(TAG, "Put json");
    }
    // 이미지 Resize 함수
    private int setSimpleSize(BitmapFactory.Options options, int requestWidth, int requestHeight){
        // 이미지 사이즈를 체크할 원본 이미지 가로/세로 사이즈를 임시 변수에 대입.
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        // 원본 이미지 비율인 1로 초기화
        int size = 1;

        // 해상도가 깨지지 않을만한 요구되는 사이즈까지 2의 배수의 값으로 원본 이미지를 나눈다.
        if(originalHeight > requestHeight || originalWidth > requestWidth) {
            final int heightRatio = Math.round((float) originalHeight / (float) requestHeight);
            final int widthRatio = Math.round((float) originalWidth / (float) requestWidth);

            size = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return size;
    }

}




























