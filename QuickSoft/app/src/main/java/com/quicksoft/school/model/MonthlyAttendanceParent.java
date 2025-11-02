package com.quicksoft.school.model;

import com.quicksoft.school.util.TimeUtil;

public class MonthlyAttendanceParent {
    private int present;
    private int holiday;
    private int month;
    private int year;
    private int percentage;
    int[] monthArray;

    public MonthlyAttendanceParent(int present, int holiday, int month, int year) {
        this.present = present;
        this.holiday = holiday;
        this.month = month;
        this.year = year;
    }

    public MonthlyAttendanceParent(int percentage, int[] monthArray, int month) {
        this.percentage = percentage;
        this.month = month;
        this.monthArray = monthArray;
    }

//    public float getPresentPercentage(){
//        float percent;
//        int workingDays = TimeUtil.getNoOfDaysInMonth(month, year) - (TimeUtil.getNoOfWeekends(month, year) + holiday);
//        percent = (float)present/workingDays;
//        //LogUtils.i("Parcentage: "+ percent + " noOfWeekend: "+ TimeUtil.getNoOfWeekends(month, year) + " noOfDays: "+ TimeUtil.getNoOfDaysInMonth(month, year));
//        return percent;
//    }

    public int getPresentPercentage(){
        return percentage;
    }

    public int getTotalDays(){
        return TimeUtil.getNoOfDaysInMonth(month, year);
    }

    public int getHolidays(){
        return holiday;
    }

    public int getPresent(){
        return present;
    }

    public int getAbsent(){
        int workingDays = TimeUtil.getNoOfDaysInMonth(month, year) - (TimeUtil.getNoOfWeekends(month, year) + holiday);
        int absent = workingDays - present;
        return absent;
    }

    public int getMonth(){
        return month;
    }
    public int[] getAttendanceForMonth(){
        return monthArray;
    }
}
