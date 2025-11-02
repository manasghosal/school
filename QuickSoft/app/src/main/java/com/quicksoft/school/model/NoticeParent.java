package com.quicksoft.school.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoticeParent {

    String id;
    String teacherName;
    String timestamp;
    int type;
    String title;
    String description;
    boolean submit;

    public NoticeParent(String id, String timestamp, String title, int type) {
        this.id = id;
        this.timestamp = timestamp;
        this.title = title;
        this.type = type;
    }

    public String getTimestamp() {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy");
            Date ddate = null;
            ddate = dt.parse(timestamp);
            String date = dt1.format(ddate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public String getNoticeId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSubmit(boolean submit) {
        this.submit = submit;
    }
    public boolean getSubmit() {
        return submit;
    }
}
