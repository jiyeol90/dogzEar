package com.example.dogzear.saveAndLoad;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.example.dogzear.dto.BookItem;
import com.example.dogzear.dto.BookMemo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import static android.content.Context.MODE_PRIVATE;

public class SaveAndLoad {
    private static final String TAG = "SaveAndLoad";
    //책 저장용
    private static final String SETTINGS_BOOK_JSON = "settings_book_json";

    public static void setStringArrayPref(Context context, String key, ArrayList<BookItem> items) {

//        for(int i = 0; i < items.size(); i++) {
//            Log.d(TAG, "setSTringArrayPref =================================================데이터 저장할때");
//            Log.d(TAG, items.size()+ " 개중 " + (i+1) +" 번째 책: " + items.get(i).getTitle() );
//            for(int j = 0 ; j < items.get(i).getMemoList().size(); j++) {
//                Log.d(TAG, "     "+ (j+1) +" 번째 메모 (페이지): " + items.get(i).getMemoList().get(j).getPage() );
//            }
//        }

        SharedPreferences prefs = context.getSharedPreferences(key, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray jsonBookArray = new JSONArray();
        JSONObject jsonBookObject;
        JSONArray jsonBookMemoArray;
        JSONObject jsonBookMemoObject;
        ArrayList <BookMemo> bookMemoArrayList;

        for(int i = 0; i < items.size(); i++) {
            //ArrayList<BookItem> items에서 하나의 item을 담는다.
            jsonBookObject = new JSONObject();
            jsonBookMemoArray = new JSONArray();
            try {
                jsonBookObject.put("image", BitMapToString(items.get(i).getImage()));
                jsonBookObject.put("title", items.get(i).getTitle());
                jsonBookObject.put("author", items.get(i).getAuthor());
                jsonBookObject.put("startDate", items.get(i).getStartDate());
                jsonBookObject.put("finishDate", items.get(i).getFinishDate());
                jsonBookObject.put("rating", items.get(i).getRating());
                jsonBookObject.put("comment", items.get(i).getComment());
                jsonBookObject.put("totalPage", items.get(i).getTotalPage());
                jsonBookObject.put("readingTime", items.get(i).getReadingTime());
                jsonBookObject.put("bookId", items.get(i).getBookId());
                bookMemoArrayList = items.get(i).getMemoList();
                //로그값으로 확인.

                for(int j = 0; j < bookMemoArrayList.size(); j++) {
                    jsonBookMemoObject = new JSONObject();
                    jsonBookMemoObject.put("page", bookMemoArrayList.get(j).getPage());
                    jsonBookMemoObject.put("chapter",bookMemoArrayList.get(j).getChapter());
                    jsonBookMemoObject.put("date", bookMemoArrayList.get(j).getDate());
                    jsonBookMemoArray.put(jsonBookMemoObject);
                }
                //bookMemoList
                jsonBookObject.put("bookMemoList", jsonBookMemoArray);

                jsonBookArray.put(jsonBookObject);
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //아이템의 개수가 0일 경우는 어떻게 처리해야 하나
//        if(!items.isEmpty()) {
//            editor.putString(key, jsonBookArray.toString());
//        }

        editor.putString(key, jsonBookArray.toString());

        Log.d(TAG, "저장할때 JSON으로 이렇게 저장한다 :  " + jsonBookArray.toString());
        //editor.apply();
        editor.commit();
    }


    //SharedPreference에서 값 가져오기
    public static ArrayList<BookItem> getStringArrayPref(Context context, String key) {

        SharedPreferences pref = context.getSharedPreferences(key, MODE_PRIVATE);
        String jsonBookStr = pref.getString(key, null);

        //Log.d(TAG, "처음 시작할때   JSON은 이렇게 String으로 저장되어 있고 이걸 그대로 배열로 변환해서 가져오는거야 .:  " + jsonBookStr);
        //JSON을 String으로 받아온다.
        if(jsonBookStr != null) {
            //Log.d(TAG, jsonBookStr);
        }
        ArrayList<BookItem> bookItems = new ArrayList<BookItem>();
        // ArrayList<Memo> bookMemoList = new ArrayList<Memo>();
        //이미 bookItems를 만들때 내부에 생성되어있다. => Book과 Memo의 관계를 형성해 주고 있으므로 Memo 리스트를 따로 생성하지 말것.
        BookMemo memo;
        BookItem item;

        if(jsonBookStr != null) {
            try {
                //전체 JSONArray를 받는다.
                JSONArray jsonBookArray = new JSONArray(jsonBookStr);
                //BookItem에서 memo에 해당하는 JSONArray
                JSONArray jsonBookMemoArray;
                //BookItem에 해당하는 JSONObject
                JSONObject jsonBookObject;
                //memo에 해당하는 JSONObject
                JSONObject jsonBookMemoObject;

                for(int i = 0; i < jsonBookArray.length(); i++) {
                    //BookArray에서 BookItem을 추출한다.
                    jsonBookObject = jsonBookArray.getJSONObject(i);
                    Bitmap image = StringToBitMap(jsonBookObject.optString("image"));
                    String title = jsonBookObject.optString("title");
                    String author = jsonBookObject.optString("author");
                    String startDate = jsonBookObject.optString("startDate");
                    String finishDate = jsonBookObject.optString("finishDate");
                    String rating = jsonBookObject.optString("rating");
                    String comment = jsonBookObject.optString("comment");
                    String totalPage = jsonBookObject.optString("totalPage");
                    String readingTime = jsonBookObject.optString("readingTime");
                    String bookId = jsonBookObject.optString("bookId");
                    //BookItem 의 memoList를 JSONArray 로 받는다.
                    jsonBookMemoArray = jsonBookObject.optJSONArray("bookMemoList");

                   // Log.d(TAG, "jsonBookMemoArray 갯수" + jsonBookMemoArray.length());
                    //메모리스트를 잘 받아오나 체크
                   // Log.d(TAG, i + "번째 책 메모리스트를 JSON형식으로 어떻게 받아오나. : " + jsonBookMemoArray.toString());

                    //밑에 주석된 곳에서 BookItem을 생성하면 안된다. 밑의 for문에서 MemoList를 담아줘야 하기때문에
                    //먼저 BookItem을 생성해야 한다. (###주의할 것###)
                    item = new BookItem();
                    item.setImage(image);
                    item.setTitle(title);
                    item.setAuthor(author);
                    item.setStartDate(startDate);
                    item.setFinishDate(finishDate);
                    item.setRating(rating);
                    item.setComment(comment);
                    item.setTotalPage(totalPage);
                    item.setReadingTime(readingTime);
                    item.setBookId(bookId);
                    bookItems.add(item);
                    //MemoArrayList에 들어있는 값을 parsing한다.
                    for(int j = 0; j < jsonBookMemoArray.length(); j++) {
                        jsonBookMemoObject = jsonBookMemoArray.getJSONObject(j);
                        String memoPage = jsonBookMemoObject.optString("page");
                        String memoChapter = jsonBookMemoObject.optString("chapter");
                        String memoDate = jsonBookMemoObject.optString("date");

                        memo = new BookMemo(memoPage,memoChapter,memoDate);
                        bookItems.get(i).getMemoList().add(memo);
                        //bookMemoList.add(memo);
                    }
                    //item.setMemoList( bookItems.get(i).getMemoList());

//                    item = new BookItem();
//                   // item.setImage(image);
//                    item.setTitle(title);
//                    item.setAuthor(author);
//                    item.setDate(date);
//                    item.setMemoList( bookItems.get(i).getMemoList());
//
//                    bookItems.add(item);
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
        //의도를 적어라
        for(int i = 0; i < bookItems.size(); i++) {
            Log.d(TAG, "getStringArrayPref =================================================데이터 불러올때");
            Log.d(TAG, bookItems.size()+ " 개중 " + (i+1) +" 번째 책: " + bookItems.get(i).getTitle() );
            for(int j = 0 ; j < bookItems.get(i).getMemoList().size(); j++) {
                Log.d(TAG, "     "+ (j+1) +" 번째 메모 (페이지): " + bookItems.get(i).getMemoList().get(j).getPage() );
            }
        }
        return bookItems;
    }



    //비트맵을 String 으로 변환하는 메소드
    public static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);

        return temp;
    }

    //encoding된 String 을 Bitmap으로 변환하는 메소드
    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

}
