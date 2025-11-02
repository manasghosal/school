package com.quicksoft.school.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Passanger  implements Parcelable {
    private String personId;
    private String fname;
    private String mname;
    private String lname;
    private String remark;
    private String imageUrl;
    public Passanger(String id, String fname, String mname, String lname, String imageUrl, String remark) {
        this.personId = id;
        this.fname = (fname == null? "":fname);
        this.mname = (mname == null? "":mname);
        this.lname = (lname == null? "":lname);
        this.remark = (remark == null? "":remark);
        this.imageUrl = (imageUrl == null? "":imageUrl);
    }
    public Passanger(Parcel in) {
        personId = in.readString();
        fname = in.readString();
        mname = in.readString();
        lname = in.readString();
        imageUrl = in.readString();
        remark = in.readString();
    }
    public String getPassangerName()
    {
        String ret = "";
        if (fname.length() > 0)
            ret = fname;
        if (mname.length() > 0)
            ret = ret + " " + mname;
        if (lname.length() > 0)
            ret = ret + " " + lname;
        return ret;
    }
    public String getPassangerRemark()
    {
        return remark;
    }
    public String getImageUrl()
    {
        return imageUrl;
    }
    public static final Creator<Passanger> CREATOR = new Creator<Passanger>() {
        @Override
        public Passanger createFromParcel(Parcel in) {
            return new Passanger(in);
        }

        @Override
        public Passanger[] newArray(int size) {
            return new Passanger[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(personId);
        parcel.writeString(fname);
        parcel.writeString(mname);
        parcel.writeString(lname);
        parcel.writeString(imageUrl);
        parcel.writeString(remark);
    }
}
