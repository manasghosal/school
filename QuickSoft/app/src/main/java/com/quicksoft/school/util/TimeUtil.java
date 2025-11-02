package com.quicksoft.school.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TimeUtil {

    public static int getNoOfWeekends(int month, int year){
        List<Date> countWeekends = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int months = cal.get(Calendar.MONTH);
        do {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
                countWeekends.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } while (cal.get(Calendar.MONTH) == months);

        return countWeekends.size();
    }

    public static int getNoOfDaysInMonth(int month, int year){
        Calendar mycal = new GregorianCalendar(year, month, 1);
        return mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String getMonthInString(int month){
            String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            return monthNames[month];
    }

    public static int getCurrentMonth(){
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    public static int getCurrentYear(){
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static String getFormattedDate(String timestamp) {
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

    public static String getReverseFormattedDate(String timestamp) {
        try {
            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
            SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
            Date ddate = null;
            ddate = dt.parse(timestamp);
            String date = dt1.format(ddate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTodaysDate() {
        SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
        String date = dt.format(new Date());
        return date;
    }
}
