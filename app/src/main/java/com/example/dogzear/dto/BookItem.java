package com.example.dogzear.dto;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BookItem implements Parcelable {
    //BookItem을 생성할때마다 고유의 값을 생성한다.
    public static int ISBN = 1000;

    private String bookId;
    private Bitmap image;
    private String title;
    private String author;
    private String startDate;
    private String finishDate;
    private String rating;
    private String comment;
    private String totalPage;
    private String readingTime;
    //한권의 책에 담길 메모들의 리스트
    private ArrayList<BookMemo> memoList;

    public BookItem() {

        bookId = Integer.toString(ISBN++); // 1번책 ID : 1001, 2번책 ID : 1002, 3번책 ID : 1003 ....
        memoList = new ArrayList<BookMemo>();
    }
    //CREATOR가 사용하는 생성자
    protected BookItem(Parcel in) {
        image = in.readParcelable(Bitmap.class.getClassLoader());
        title = in.readString();
        author = in.readString();
        startDate = in.readString();
        finishDate = in.readString();
        rating = in.readString();
        comment = in.readString();
        totalPage = in.readString();
        readingTime = in.readString();
        bookId = in.readString();
        memoList = new ArrayList<BookMemo>(); //이줄이 없으면 실행되지 않는다.
        //memoList = in.createTypedArrayList(Memo.CREATOR);
        in.readTypedList(memoList, BookMemo.CREATOR);
    }

    public static final Creator<BookItem> CREATOR = new Creator<BookItem>() {
        @Override
        public BookItem createFromParcel(Parcel in) {
            return new BookItem(in);
        }

        @Override
        public BookItem[] newArray(int size) {
            return new BookItem[size];
        }
    };

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setFinishDate(String finishDate) { this.finishDate = finishDate; }

    public void setRating(String rating) {this.rating = rating;}

    public void setComment(String comment) {this.comment = comment;}

    public void setBookId(String bookId) {this.bookId = bookId;}

    public void setMemoList(ArrayList<BookMemo> memoList) {
        this.memoList = memoList;
    }

    public void setTotalPage(String totalPage) { this.totalPage = totalPage; }

    public void setReadingTime(String readingTime) { this.readingTime = readingTime; }

    public Bitmap getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getFinishDate() { return finishDate; }

    public String getRating() {return rating;}

    public String getComment() {return comment;}

    public String getTotalPage() { return totalPage; }

    public String getReadingTime() { return readingTime; }

    public String getBookId() {return bookId;}

    public ArrayList<BookMemo> getMemoList() {
        return memoList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //writeToParce에서 보내는 변수 순서대로 읽어야 한다.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(image, flags);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(startDate);
        dest.writeString(finishDate);
        dest.writeString(rating);
        dest.writeString(comment);
        dest.writeString(totalPage);
        dest.writeString(readingTime);
        dest.writeString(bookId);
        dest.writeTypedList(memoList);
    }
}
