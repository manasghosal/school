package com.quicksoft.school.model;

public class TaskTeacher {
    private String taskTile;
    private String taskDate;
    private String taskClass;
    private String taskSection;
    private String taskSubject;
    private String taskDescription;
    private String studentName;
    private int orderId;

    public TaskTeacher(int orderId, String taskTile, String taskDate, String taskClass, String taskSection, String taskSubject, String taskDescription) {
        this.taskTile = taskTile;
        this.taskDate = taskDate;
        this.taskClass = taskClass;
        this.taskSection = taskSection;
        this.taskSubject = taskSubject;
        this.taskDescription = taskDescription;
        this.orderId = orderId;
    }

    public TaskTeacher(String studentName, String taskTile, String taskDate, String taskClass, String taskSection, String taskSubject, String taskDescription) {
        this.taskTile = taskTile;
        this.taskDate = taskDate;
        this.taskClass = taskClass;
        this.taskSection = taskSection;
        this.taskSubject = taskSubject;
        this.taskDescription = taskDescription;
        this.studentName = studentName;
    }

    public String getTaskTile() {
        return taskTile;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public String getTaskClass() {
        return taskClass;
    }

    public String getTaskSection() {
        return taskSection;
    }

    public String getTaskSubject() {
        return taskSubject;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getTaskStudentName() {
        return studentName;
    }

    public int getOrderId() {
        return orderId;
    }
}
