package com.liwang.bean;

/**
 * Created by Nikolas on 2015/5/23.
 */
//首页信息bean
public class HomePage implements Comparable<HomePage> {

    private String title;

    private String url;

    private String date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        return hashCode()==o.hashCode();
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public int compareTo(HomePage another) {
        return -getDate().compareTo(another.getDate());
    }
}
