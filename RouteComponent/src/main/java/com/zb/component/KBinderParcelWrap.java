package com.zb.component;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class KBinderParcelWrap implements Parcelable {
    public IBinder binder;

    public  KBinderParcelWrap(){

    }
    public  KBinderParcelWrap(IBinder binder){
        this.binder = binder;
    }
    public KBinderParcelWrap(Parcel in){
        readFromParcel(in);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in){
        binder = in.readStrongBinder();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(binder);
    }
    public static final Creator<KBinderParcelWrap> CREATOR = new Creator<KBinderParcelWrap>() {
        @Override
        public KBinderParcelWrap createFromParcel(Parcel in) {
            return new KBinderParcelWrap(in);
        }

        @Override
        public KBinderParcelWrap[] newArray(int size) {
            return new KBinderParcelWrap[size];
        }
    };
}
