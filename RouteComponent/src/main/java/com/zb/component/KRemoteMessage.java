package com.zb.component;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class KRemoteMessage implements Parcelable {
    public String action;

    public Bundle data;
    public KRemoteMessage(String action){
        this.action = action;
        this.data = new Bundle();
    }

    public KRemoteMessage(Parcel in){
        readFromParcel(in);
    }
    public KRemoteMessage(){
        this.action = "";
        this.data = new Bundle();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.action);
        dest.writeBundle(data);
    }
    public void readFromParcel(Parcel in){
        this.action = in.readString();
        this.data = in.readBundle(getClass().getClassLoader());
    }
    public static final Creator<KRemoteMessage> CREATOR = new Creator<KRemoteMessage>() {
        @Override
        public KRemoteMessage createFromParcel(Parcel in) {
            return new KRemoteMessage(in);
        }

        @Override
        public KRemoteMessage[] newArray(int size) {
            return new KRemoteMessage[size];
        }
    };

}
