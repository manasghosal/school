package com.quicksoft.school.model;

public class ScheduleTeacher {
    private String scheduleTitle;
    private String time;
    private String classRoom;
    private int orderId;

    public ScheduleTeacher(int orderId, String scheduleTitle, String time, String classRoom) {
        this.scheduleTitle = scheduleTitle;
        this.time = time;
        this.classRoom = classRoom;
        this.orderId = orderId;
    }

    public ScheduleTeacher(String scheduleTitle, String time, String classRoom) {
        this.scheduleTitle = scheduleTitle;
        this.time = time;
        this.classRoom = classRoom;
    }

    public String getScheduleTitle() {
        return scheduleTitle;
    }

    public String getTime() {
        return time;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public int getOrderId() {
        return orderId;
    }
}
