package com.quicksoft.school.model;

import android.os.Parcel;
import android.os.Parcelable;


public class AttendanceStudent implements Parcelable {

    private int rollNo;
    private String name;
    private String imageUrl;
    private String studentId;
    private String remark;
    private boolean checked;

    public AttendanceStudent(int rollNo, String name, String imageUrl, String studentId, String remark) {
        this.name = name;
        this.rollNo = rollNo;
        this.imageUrl = imageUrl;
        this.studentId = studentId;
        this.remark = remark;
    }

    public AttendanceStudent(int rollNo, String name, String imageUrl, String studentId) {
        this.name = name;
        this.rollNo = rollNo;
        this.imageUrl = imageUrl;
        this.studentId = studentId;
    }

    public AttendanceStudent(int rollNo, String name, String imageUrl) {
        this.name = name;
        this.rollNo = rollNo;
        this.imageUrl = imageUrl;
        this.studentId = studentId;
    }

    public AttendanceStudent(Parcel in) {
        name = in.readString();
        rollNo = in.readInt();
        imageUrl = in.readString();
        studentId = in.readString();
    }

    public int getRollNo() {
        return rollNo;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean getChecked() {
        return checked;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public static final Creator<AttendanceStudent> CREATOR = new Creator<AttendanceStudent>() {
        @Override
        public AttendanceStudent createFromParcel(Parcel in) {
            return new AttendanceStudent(in);
        }

        @Override
        public AttendanceStudent[] newArray(int size) {
            return new AttendanceStudent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(rollNo);
        parcel.writeString(imageUrl);
        parcel.writeString(studentId);
    }
}
