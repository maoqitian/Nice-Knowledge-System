package com.mao.aidlapplication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : maoqitian
 * date : 2021/8/22 15:41
 * description :
 */
public class ParData implements Parcelable {

    private int data1;
    private String data2;

    public ParData(){

    }

    protected ParData(Parcel in) {
        data1 = in.readInt();
        data2 = in.readString();
    }

    public static final Creator<ParData> CREATOR = new Creator<ParData>() {
        @Override
        public ParData createFromParcel(Parcel in) {
            return new ParData(in);
        }

        @Override
        public ParData[] newArray(int size) {
            return new ParData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(data1);
        parcel.writeString(data2);
    }

    public int getData1() {
        return data1;
    }

    public void setData1(int data1) {
        this.data1 = data1;
    }

    public String getData2() {
        return data2;
    }

    public void setData2(String data2) {
        this.data2 = data2;
    }

    @Override
    public String toString() {
        return "ParData{" +
                "data1=" + data1 +
                ", data2='" + data2 + '\'' +
                '}';
    }
}
