package com.example.dogzear.dto;

public class MemoItem {
    private String title;
    private String content;
    private String page;

    public MemoItem(String title, String content, String page) {
        this.title = title;
        this.content = content;
        this.page = page;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getPage() {
        return page;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPage(String page) {
        this.page = page;
    }

}
