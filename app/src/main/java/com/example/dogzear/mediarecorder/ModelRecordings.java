package com.example.dogzear.mediarecorder;
import android.net.Uri;

public class ModelRecordings {

    //파일 제목
    String title;
    //파일 길이
    String duration;
    //파일 날짜
    String date;
    Uri uri;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

}