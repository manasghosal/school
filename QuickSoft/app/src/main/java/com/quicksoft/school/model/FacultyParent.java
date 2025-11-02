package com.quicksoft.school.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FacultyParent {
    private int id;
    private String name;
    private String designation;
    private String imageProfile;
    private String education;
    private String jobType;
    private String phone;
    private String lastVisit;

    public FacultyParent(int id, String name, String designation, String imageProfile, String education, String jobType, String phone, String lastVisit) {
        this.id = id;
        this.name = name;
        this.designation = designation;
        this.imageProfile = imageProfile;
        this.education = education;
        this.jobType = jobType;
        this.phone = phone;
        this.lastVisit = lastVisit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesignation() {
        return designation;
    }

    public String getEducation() {
        return education;
    }

    public String getJobType() {
        return jobType;
    }

    public String getPhone() {
        return phone;
    }

    public String getLastVisit() {
        if(lastVisit!=null) {
            try {
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
                SimpleDateFormat dt1 = new SimpleDateFormat("HH:mm");
                Date ddate = null;
                ddate = dt.parse(lastVisit);
                String date = dt1.format(ddate);
                return date;
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    public String getImageProfile() {
        return imageProfile;
    }
}
