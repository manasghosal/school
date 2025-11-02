package com.quicksoft.school;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.orhanobut.hawk.Hawk;
import com.quicksoft.school.model.AttendanceStudent;

import java.util.ArrayList;


public class Application extends android.app.Application {


    @Override
    public void onCreate() {
        super.onCreate();

        Utils.init(getApplicationContext());
        LogUtils.Config mConfig = LogUtils.getConfig();
        mConfig.setLogSwitch(true)
                .setConsoleSwitch(true)
                .setGlobalTag(getString(R.string.logTag))
                .setBorderSwitch(true)
                .setConsoleFilter(LogUtils.V);

        Hawk.init(getApplicationContext()).build();
        Iconify.with(new MaterialModule());
    }

    ArrayList<AttendanceStudent> attendanceStudentArrayList=null;
    public void setStudentAttendanceData(ArrayList<AttendanceStudent> attendanceStudentArrayList){
        this.attendanceStudentArrayList = attendanceStudentArrayList;
    }

    public ArrayList<AttendanceStudent> getStudentAttendanceData(){
        return attendanceStudentArrayList;
    }
}

