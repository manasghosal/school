package com.quicksoft.school.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskParent {

    String id;
    String teacherName;
    String timestamp;
    String dueTimestamp;
    String title;
    String description;
    boolean submit;

//

    public TaskParent(String id, String timestamp, String dueTimestamp, String title, String description) {
        this.id = id;
        this.dueTimestamp = dueTimestamp;
        this.timestamp = timestamp;
        this.title = title;
        this.description = description;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public String getDueTimestamp() {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy");
            Date ddate = null;
            ddate = dt.parse(dueTimestamp);
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskId() {
        return id;
    }

    public void setSubmit(boolean submit) {
        this.submit = submit;
    }
    public boolean getSubmit() {
        return submit;
    }
}
