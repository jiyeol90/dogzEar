package com.example.dogzear.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class BookMemo implements Parcelable {

    private String page ;
    private String chapter ;
    private String date ;

    //인자 생성자
    public BookMemo(String page, String chapter, String date) {
        this.page = page;
        this.chapter = chapter;
        this.date = date;
    }

    //CREATOR가 사용하는 생성자
    protected BookMemo(Parcel in) {
        page = in.readString();
        chapter = in.readString();
        date = in.readString();
    }

    public static final Creator<BookMemo> CREATOR = new Creator<BookMemo>() {
        @Override
        public BookMemo createFromParcel(Parcel in) {
            return new BookMemo(in);
        }

        @Override
        public BookMemo[] newArray(int size) {
            return new BookMemo[size];
        }
    };

    public String getPage() {
        return page;
    }

    public String getChapter() {
        return chapter;
    }

    public String getDate() {
        return date;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public void setDate(String date) {
        this.date = date;
    }

    //FILEDESCRIPTOR일 경우 외에는 수정할 필요 없다.
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(page);
        dest.writeString(chapter);
        dest.writeString(date);
    }
}
