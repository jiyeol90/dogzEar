package com.example.dogzear;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogzear.adapter.BookMemoAdapter;
import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.lottie.LottieAddBookDialogFragment;
import com.example.dogzear.lottie.LottieDeleteBookDialogFragment;
import com.example.dogzear.saveAndLoad.SaveAndLoad;
import com.example.dogzear.dto.BookItem;
import com.example.dogzear.dto.BookMemo;

import java.util.ArrayList;

/*
이 파일은 북 정보를 알려주는 액티비티이다.
이 액티비티에는 책을 조회할 수 있는 기능과 특정 책의 정보를 알수 있다.
1. 책 목록 조회
2. 책 상세 조회

 */
public class BookInfoActivity extends BaseActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener{
    private static final String TAG = "BookInfoActivity";

    BookItem item;
    //각 책에 할당된 메모들의 리스트
    private ArrayList<BookMemo> memoList;
    private ArrayList<BookItem> listViewItemList;
    private BookMemoAdapter memoAdapter;
    private int count = -1;
    private static final String SETTINGS_BOOK_JSON = "settings_book_json";
//책 목록 조회

    private RecyclerView memoRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private ImageView bookImage;
    private ImageView here;
    private TextView totalBookPage;
    private TextView bookTimer;
    private Button deleteBtn;
    private Button bookModifyBtn;
    private Button memoInsertBtn;
    private Button completeBtn;
    private Handler mhandler;

    private EditText bookTitle;
    private EditText bookAuthor;
    private EditText bookDate;

    private ProgressBar readingProgress;

    //필터링된 리스트 아이템의 인덱스 (수정, 삭제할 경우 인덱스 정보를 이용한다.)
    int filteredPosition;
    //원본 리스트 아이템의 인덱스
    int originPosition;
    //책 item의 고유ID
    String bookId;
    int currentBookPage;
    long recordTime;
    String recordTimeStr;
    Handler processHandler;

    private String title;
    private String author;
    private String date;
    private String totalPage;
    private Bitmap image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        Intent intent = getIntent();

        //인텐트에 저장된 객체를 가져온다.
        //item = (BookItem)intent.getParcelableExtra("item");
        //필터링된 ArrayList에서의 인덱스값 -> 쓸모 없다.//todo 이해할 수 있게....
        filteredPosition = intent.getIntExtra("position", -1);
        //책의 고유값을 얻는다.
        bookId = intent.getStringExtra("bookId");
        listViewItemList = SaveAndLoad.getStringArrayPref(this, SETTINGS_BOOK_JSON);

        //bookId가 같은 인덱스를 구한다. -> 필터되지 않는 ArrayList에서의 인덱스를 구한다. ****
        for(int i = 0; i < listViewItemList.size(); i++) {
            if(listViewItemList.get(i).getBookId().equals(bookId)) {
                originPosition = i;
            }
        }

        item = listViewItemList.get(originPosition);

        //리사이클러뷰 설정
        memoRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_memo_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        memoRecyclerView.setLayoutManager(linearLayoutManager);

        memoList = item.getMemoList();
        memoAdapter = new BookMemoAdapter(this, memoList);
        memoRecyclerView.setAdapter(memoAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(memoRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        memoRecyclerView.addItemDecoration(dividerItemDecoration);



        totalBookPage = (TextView) findViewById(R.id.total_page);
        bookTimer = (TextView) findViewById(R.id.timer);
        //here = (ImageView) findViewById(R.id.here);
        bookImage = (ImageView) findViewById(R.id.search_bar);
        deleteBtn = (Button) findViewById(R.id.delete_btn);
        bookModifyBtn = (Button) findViewById(R.id.book_modify_btn);
        completeBtn = (Button) findViewById(R.id.complete_btn);
        memoInsertBtn = (Button) findViewById(R.id.memo_insert);
        bookTitle = (EditText) findViewById(R.id.et_title);
        bookAuthor = (EditText) findViewById(R.id.et_author);
        bookDate = (EditText) findViewById(R.id.et_date);

        int totalBookPage = Integer.parseInt(item.getTotalPage());
        readingProgress = (ProgressBar) findViewById(R.id.progress);
        //프로그래스바의 Max값을 해당 책의 총 페이지수로 세팅한다.
        readingProgress.setMax(totalBookPage);
        this.totalBookPage.setText(totalBookPage+"p");

        //작성된 독서진행 메모가 있을경우 진행정도를 표시해준다.
        if(item.getMemoList().size() != 0) {
            //기존에 입력된 메모리스트에서 [페이지 숫자 + "p"] 로 표기된 String 에서 문자열 "p"를 제외한 문자를 얻는다.
            String extractPage = item.getMemoList().get(0).getPage().split("p")[0];
            //숫자로표기된 문자열을 숫자로 변환한다.
            currentBookPage = Integer.parseInt(extractPage);
            //readingProgress.setProgress(currentBookPage);
        }

        deleteBtn.setOnClickListener(this);
        bookModifyBtn.setOnClickListener(this);
        memoInsertBtn.setOnClickListener(this);
        completeBtn.setOnClickListener(this);
        bookDate.setOnClickListener(this);
        bookTimer.setOnClickListener(this);

        Log.d(TAG, "책 리스트의 개수 :  "+listViewItemList.size() +
                "\n " + (originPosition + 1) + "번째 책 메모의 수" + memoAdapter.getMemoItemList().size());

        //                Bitmap image = item.getImage();
//                image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//                String title = item.getTitle();
//                String author = item.getAuthor();
//                String date = item.getDate();
//
//                intent.putExtra("image", byteArray);
//                intent.putExtra("title", title);
//                intent.putExtra("author", author);
//                intent.putExtra("date", date);
        ///////////////////////////////////////////////////////////////////////

       // byte[] byteArray = intent.getByteArrayExtra("image");

//        image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//        bookImage.setImageBitmap(image);
//        title = intent.getStringExtra("title");
//        bookTitle.setText(title);
//        author = intent.getStringExtra("author");
//        bookAuthor.setText(author);
//        date = intent.getStringExtra("date");
//        bookDate.setText(date);
//        position = intent.getIntExtra("position", -1);

        bookImage.setImageBitmap(item.getImage());
        bookTitle.setText(item.getTitle());
        bookAuthor.setText(item.getAuthor());
        bookDate.setText(item.getStartDate());

        if(!item.getReadingTime().equals("")) {
            recordTime = Long.parseLong(item.getReadingTime());
            recordTimeStr = String.format("%02d:%02d:%02d", recordTime / 1000 / 60 / 60, recordTime / 1000 / 60, (recordTime / 1000) % 60);
            bookTimer.setText(recordTimeStr);
        }

        //이 코드를 위에서 지정하므로 여기선 쓰지 않는다.
        //memoList = item.getBookMemoList();

       // Log.d(TAG, "총 개수 : "  + memoList.get(0).getChapter());
        if(memoList.size() != 0) {
            for (int i = 0; i < memoList.size(); i++) {
                Log.d(TAG, i + " 번째   : " + memoList.get(i));
            }
        }
        Toast.makeText(this, "내가 택한 아이템은 " + (originPosition+1)  + "번째 아아템이다. ", Toast.LENGTH_SHORT).show();

        //책 읽은 과정을 나타내는 프로그래스바에 애니메이션 효과를 주기위한 핸들러
        processHandler = new Handler();

        Thread ProgressThread = new ProgressThread();
        ProgressThread.start();
    }

    //프로그래스 바에 따라 움직이는 화살표 -> 추후에 수정할것.
    class ProgressThread extends Thread {
        Handler handler = processHandler;
        int process = 0;
        @Override
        public void run() {
            //상태바가 0 부터 내가 현재 읽은 페이지까지 0.01초 간격으로 1씩 증가한다.
            while(process < currentBookPage) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        readingProgress.setProgress(process++);
                        //here.setX(readingProgress.getProgress() * 2 * Math.ceil( (double)currentBookPage) / readingProgress.getMax());
                        //Todo 화살표 움직임 공식 계산해볼것.
                    }
                };
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace() ;
                }
                handler.post(runnable);
            }
        }
    }

    //onResume에서 시간을 표기해 준다.


    @Override
    protected void onResume() {
        super.onResume();
        listViewItemList = SaveAndLoad.getStringArrayPref(this, SETTINGS_BOOK_JSON);
        item = listViewItemList.get(originPosition);

        if(!item.getReadingTime().equals("")) {
            recordTime = Long.parseLong(item.getReadingTime());
            recordTimeStr = String.format("%02d:%02d:%02d", recordTime / 1000 / 60 / 60, recordTime / 1000 / 60, (recordTime / 1000) % 60);
            bookTimer.setText(recordTimeStr);
        }
    }

    @Override
    protected void onPause() {
        listViewItemList.get(originPosition).setMemoList(memoAdapter.getMemoItemList());
        Log.d(TAG, "책 리스트의 개수 :  "+listViewItemList.size() +
                "\n " + (originPosition + 1) + "번째 책 메모의 수" + memoAdapter.getMemoItemList().size());
        SaveAndLoad.setStringArrayPref(this,SETTINGS_BOOK_JSON, listViewItemList);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        //Variable 'resultIntent' is accessed from within inner class, needs to be declared final
        //final로 선언해야 하는 이유  출처 : https://dreamaz.tistory.com/259
        final Intent resultIntent = new Intent();

        /*수정하기 버튼을 누를시 정보를 보낸다.(어디로 , 어떤 정보를 보낸는가.)*/
        if(view == bookModifyBtn) {
            //수정을 누르면 수정가능한 상태로 바뀌고 수정을 한 후
            //확인 버튼을 누르면 수정이 된다.
            if(bookModifyBtn.getText().equals("수정")) {
                bookModifyBtn.setText("확인");
                bookTitle.setEnabled(true);
                bookAuthor.setEnabled(true);
                bookDate.setEnabled(true);

            } else if (bookModifyBtn.getText().equals("확인")) {
                item.setTitle(bookTitle.getText().toString());
                item.setAuthor(bookAuthor.getText().toString());
                item.setStartDate(bookDate.getText().toString());
                item.setMemoList(memoList);
                resultIntent.putExtra("item", item);
                resultIntent.putExtra("RESULT", "MODIFY");
                resultIntent.putExtra("position", originPosition);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
            //수정한 값을 Item에 담는다.
//            item.setTitle(bookTitle.getText().toString());
//            item.setAuthor(bookAuthor.getText().toString());
//            item.setDate(bookDate.getText().toString());
//            item.setMemoList(memoList);

            //Item객체를 담는다.
//            resultIntent.putExtra("item", item);
//            resultIntent.putExtra("RESULT", "MODIFY");
//            resultIntent.putExtra("position", position);
//            setResult(RESULT_OK, resultIntent);
//            finish();
        }else if(view == deleteBtn) {
            //삭제할 인덱스만 보내주고 LibraryActivity에서 삭제한다.//Todo(추측하지 않게 적을것.)
            AlertDialog.Builder builder = new AlertDialog.Builder(BookInfoActivity.this);
            builder.setIcon(R.drawable.book);
            builder.setTitle("알림");
            builder.setMessage("정말 삭제 하시겠습니까?");
            builder.setNegativeButton("아니오", null);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    resultIntent.putExtra("RESULT", "DELETE");
                    resultIntent.putExtra("position", originPosition);
                    setResult(RESULT_OK, resultIntent);
                    showProgressDialog();
                    mhandler = new Handler(Looper.getMainLooper());
                    new DeleteBookLoadingThread().start();
                    //finish();
                }
            });
            builder.create().show();

            //listViewItemList.remove(position);  // onPause()에서의 처리가 겹치므로 데이터를 넘겨서 처리하자
//            resultIntent.putExtra("RESULT", "DELETE");
//            resultIntent.putExtra("position", position);
//            setResult(RESULT_OK, resultIntent);
//            finish();

        }else if(view == memoInsertBtn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(BookInfoActivity.this);
            View memoView = LayoutInflater.from(BookInfoActivity.this).inflate(R.layout.book_memo_box, null, false);
            builder.setView(memoView);

            final Button submitBtn = (Button) memoView.findViewById(R.id.button_ok);
            final EditText editTextPage = (EditText) memoView.findViewById(R.id.et_page);
            final EditText editTextChapter = (EditText) memoView.findViewById(R.id.et_chapter);
            final EditText editTextDate = (EditText) memoView.findViewById(R.id.et_date);

            //데이터를 처음 넣을때는 "삽입", 기존 데이터를 수정할때는 "수정" 으로 바꿔준다.
            submitBtn.setText("삽입");

            final AlertDialog dialog = builder.create();

            submitBtn.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    String page = editTextPage.getText().toString() + "p";
                    String chapter = editTextChapter.getText().toString();
                    String date = editTextDate.getText().toString();

                    BookMemo memo = new BookMemo(page, chapter, date);
                    memoList.add(0, memo); //새로 추가된 메모는 최신순으로 보여줘야 하기때문에 가장 첫번째 인덱스로 추가한다.

                    memoAdapter.notifyItemInserted(0);
                    //MemoAdatper에 반영한 후 프로그래스바에 진행정도를 표시한다.
                    //메모는 추가하는 순간 바로 저장이되는 것이 아니므로 item.getMemoList()로 접근하면 안된다.
                    //String extractPage = item.getMemoList().get(0).getPage().split("p")[0];
                    String extractPage = memoList.get(0).getPage().split("p")[0];
                    currentBookPage = Integer.parseInt(extractPage);
                    //readingProgress.setProgress(currentBookPage);
                    Thread ProgressThread = new ProgressThread();
                    ProgressThread.start();

                    dialog.dismiss();
                }
            });
            dialog.show();
            //독서완료 버튼을 눌렀을 경우 -> dialog로 확인창 띄운후 -> 작성 페이지이동
        } else if(view == completeBtn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(BookInfoActivity.this);
            builder.setIcon(R.drawable.book);
            builder.setTitle("축! 완독!!");
            builder.setMessage("리뷰작성을 해야 완료가 됩니다. 리뷰작성을 하시겠습니까?");
            builder.setNegativeButton("아니오", null);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
                    intent.putExtra("position", originPosition);
                    //리뷰작성후 돌아올 페이지는 LibrarayActivity 이기때문에 플래그를 설정해 준다.
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(intent);
                    finish();
                }
            });
            builder.create().show();

            //책 읽기 시작한 날짜를 수정해준다.
        } else if(view == bookDate) {
            //기존의 날짜값(YY/MM/DD)의 형태를 '/'를 기준으로 나눈다.
            String[] selected = bookDate.getText().toString().split("/");
            //날짜에 해당하는 달력을 띄어준다.
            DatePickerDialog datePicker = new DatePickerDialog(this, this, Integer.parseInt(selected[0]), Integer.parseInt(selected[1])-1, Integer.parseInt(selected[2]));
            datePicker.show();

        } else if(view == bookTimer) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("북 타이머").setMessage("타이머를 시작하시겠습니까?");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    Toast.makeText(getApplicationContext(), "타이머를 시작합니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), TimerActicity.class);
                    intent.putExtra("bookId", bookId);
                    startActivity(intent);
                }
            });

            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    Toast.makeText(getApplicationContext(), "Cancel Click", Toast.LENGTH_SHORT).show();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    //로딩 애니메이션 화면을 보여주기위해 finish()의 호출을 Thread.sleep()의 시간만큼 지연시킨다.
    class DeleteBookLoadingThread extends Thread {
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        bookDate.setText(year+"/"+(month+1)+"/"+dayOfMonth);
    }

    private void showProgressDialog(){
        new LottieDeleteBookDialogFragment().newInstance().
                show(getSupportFragmentManager(),"");
    }

}












