package com.zb.component;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import java.io.Serializable;
import java.lang.reflect.Array;

public class KCallArgWrap implements Parcelable {
    public final static int TYPE_CALLBACK = 1;

    public final static int TYPE_NULL = 2;
    public final static int TYPE_INT = 3;
    public final static int TYPE_BYTE = 4;
    public final static int TYPE_SHORT = 5;
    public final static int TYPE_LONG = 6;
    public final static int TYPE_CHAR = 7;
    public final static int TYPE_FLOAT = 8;
    public final static int TYPE_DOUBLE = 9;
    public final static int TYPE_BOOL = 10;


    public final static int TYPE_INT_ARR = 11;
    public final static int TYPE_BYTE_ARR = 12;
    public final static int TYPE_SHORT_ARR = 13;
    public final static int TYPE_LONG_ARR = 14;
    public final static int TYPE_CHAR_ARR = 15;
    public final static int TYPE_FLOAT_ARR = 16;
    public final static int TYPE_DOUBLE_ARR = 17;
    public final static int TYPE_BOOL_ARR = 18;

    public final static int TYPE_Serializable = 19;
    public final static int TYPE_Parcelable = 20;
    public final static int TYPE_Serializable_ARR = 21;
    public final static int TYPE_Parcelable_ARR = 22;

    public final static int TYPE_StringArr = 23;

    public int type;
    public IRemoteCallback cb;
    public Class interfaceClazz;
    public Object obj;
    public Class objType;
    public KCallArgWrap(IRemoteCallback callback, Class interfaces){
        this.type = TYPE_CALLBACK;
        this.cb = callback;
        this.interfaceClazz = interfaces;
    }
    public KCallArgWrap(Class argType,Object arg){
        if(arg==null){
            this.type  = TYPE_NULL;
        }else if(argType.getName().equals("[I")){
            this.type  = TYPE_INT_ARR;
        }else if(argType.getName().equals("[B")){
            this.type  = TYPE_BYTE_ARR;
        }else if(argType.getName().equals("[S")){
            this.type  = TYPE_SHORT_ARR;
        }else if(argType.getName().equals("[J")){
            this.type  = TYPE_LONG_ARR;
        }else if(argType.getName().equals("[C")){
            this.type  = TYPE_CHAR_ARR;
        }else if(argType.getName().equals("[F")){
            this.type  = TYPE_FLOAT_ARR;
        }else if(argType.getName().equals("[D")){
            this.type  = TYPE_DOUBLE_ARR;
        }else if(argType.getName().equals("[Z")){
            this.type  = TYPE_BOOL_ARR;
        }else if(argType.getName().equals("[Ljava.lang.String;")){
            this.type = TYPE_StringArr;
        }else if(argType.getName().equals("int")){
            this.type  = TYPE_INT;
        }else if(argType.getName().equals("byte")){
            this.type  = TYPE_BYTE;
        }else if(argType.getName().equals("short")){
            this.type  = TYPE_SHORT;
        }else if(argType.getName().equals("long")){
            this.type  = TYPE_LONG;
        }else if(argType.getName().equals("char")){
            this.type  = TYPE_CHAR;
        }else if(argType.getName().equals("float")){
            this.type  = TYPE_FLOAT;
        }else if(argType.getName().equals("double")){
            this.type  = TYPE_DOUBLE;
        }else if(argType.getName().equals("boolean")){
            this.type  = TYPE_BOOL;
        }else if(argType.isArray()){
            Object[] arr = (Object[])arg;
            boolean isAllSerializable = true;
            boolean isAllParcelable = true;
            for (Object item:arr){
                if(item instanceof Serializable){
                    isAllParcelable = false;
                }else if(item instanceof Parcelable){
                    isAllSerializable = false;
                }else {
                    isAllParcelable = false;
                    isAllSerializable = false;
                }
            }
            if(!isAllParcelable && !isAllSerializable){
                throw new IllegalArgumentException();
            }
            if(isAllParcelable){
                this.type = TYPE_Parcelable_ARR;
            }
            if(isAllSerializable){
                this.type = TYPE_Serializable_ARR;
            }
        }else {
            if(!(arg instanceof Serializable) && !(arg instanceof Parcelable) ){
                throw new IllegalArgumentException();
            }
            if(arg instanceof  Serializable){
                this.type = TYPE_Serializable;
            }
            if(arg instanceof Parcelable){
                this.type = TYPE_Parcelable;
            }
        }
        //Log.e("RouteTest1",">>wraparg "+this.type+" arg:"+arg+" argType:"+argType.getName());
        this.obj = arg;
        this.objType = argType;
    }
    public KCallArgWrap(){
    }
    public KCallArgWrap(Parcel in){
        readFromParcel(in);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in){
        type = in.readInt();
        if(type == TYPE_CALLBACK){
            cb = IRemoteCallback.Stub.asInterface(in.readStrongBinder());
            String interfaceStrs = in.readString();
            try {
                interfaceClazz = Class.forName(interfaceStrs);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(type == TYPE_INT_ARR){
            obj = in.createIntArray();
        }else if(type == TYPE_BYTE_ARR){
            obj = in.createByteArray();
        }else if(type == TYPE_SHORT_ARR){
            int[] intArr = in.createIntArray();
            short[] sarr =new short[intArr.length];
            for (int i=0;i<intArr.length;i++){
                sarr[i] = (short) intArr[i];
            }
            obj = sarr;
        }else if(type == TYPE_LONG_ARR){
            obj = in.createLongArray();
        }else if(type == TYPE_CHAR_ARR){
            obj = in.createCharArray();
        }else if(type == TYPE_FLOAT_ARR){
            obj = in.createFloatArray();
        }else if(type == TYPE_DOUBLE_ARR){
            obj = in.createDoubleArray();
        }else if(type == TYPE_BOOL_ARR){
            obj = in.createBooleanArray();
        }else if(type == TYPE_StringArr){
            obj = in.createStringArray();
        }else if(type == TYPE_INT){
            obj = in.readInt();
        }else if(type == TYPE_BYTE){
            obj = in.readByte();
        }else if(type == TYPE_SHORT){
           obj = (short)in.readInt();
        }else if(type == TYPE_LONG){
            obj = in.readLong();
        }else if(type == TYPE_CHAR){
            obj = (char)in.readSerializable();
        }else if(type == TYPE_FLOAT){
            obj = in.readFloat();
        }else if(type == TYPE_DOUBLE){
            obj = in.readDouble();
        }else if(type == TYPE_BOOL){
            obj = (in.readInt()==1);
        }else if(type == TYPE_Serializable){
            obj = in.readSerializable();
        }else if(type == TYPE_Serializable_ARR){
            int type = in.readInt();
            if(type ==0){
                obj = null;
            }else {
                int len = in.readInt();
                String arrClassArr = in.readString();
                Class itemClazz = getArrayItemClass(arrClassArr);
                Object arr = Array.newInstance(itemClazz,len);
                for (int i=0;i<len;i++){
                    int itemtype = in.readInt();
                    if(itemtype ==0){
                        Array.set(arr,i,null);
                    }else{
                        String itemDetailClazz = in.readString();
                        Object item = in.readSerializable();
                        Array.set(arr,i,item);
                    }
                }
                obj = arr;
            }
        }else if(type == TYPE_Parcelable){
            obj = in.readParcelable(getClass().getClassLoader());
        }else if(type == TYPE_Parcelable_ARR){
            obj = in.readParcelableArray(getClass().getClassLoader());
        }

    }

    private Class getArrayItemClass(String arrClazzName){
        if(arrClazzName.startsWith("[L") && arrClazzName.endsWith(";")){
            String itemClazz = arrClazzName.substring(2,arrClazzName.length()-1);
            //LogUtil.e("ArrayTest","itemClazz:"+itemClazz);
            try {
                Class clazz = Class.forName(itemClazz);
                return clazz;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        if(type == TYPE_CALLBACK){
            dest.writeStrongBinder(cb.asBinder());
            dest.writeString(interfaceClazz.getName());
        }else if(type == TYPE_INT_ARR){
            dest.writeIntArray((int[])obj);
        }else if(type == TYPE_BYTE_ARR){
            dest.writeByteArray((byte[])obj);
        }else if(type == TYPE_SHORT_ARR){
            short[] barr = (short[])obj;
            int[]  iarr = new int[barr.length];
            for (int i=0;i<barr.length;i++){
                iarr[i] = barr[i];
            }
            dest.writeIntArray(iarr);
        }else if(type == TYPE_LONG_ARR){
            dest.writeLongArray((long[])obj);
        }else if(type == TYPE_CHAR_ARR){
            dest.writeCharArray((char[])obj);
        }else if(type == TYPE_FLOAT_ARR){
            dest.writeFloatArray((float[])obj);
        }else if(type == TYPE_DOUBLE_ARR){
            dest.writeDoubleArray((double[])obj);
        }else if(type == TYPE_BOOL_ARR){
            dest.writeBooleanArray((boolean[])obj);
        }else if(type == TYPE_StringArr){
            dest.writeStringArray((String[])obj);
        }else if(type == TYPE_INT){
            dest.writeInt((int)obj);
        }else if(type == TYPE_BYTE){
            dest.writeByte((byte)obj);
        }else if(type == TYPE_SHORT){
            dest.writeInt((short)obj);
        }else if(type == TYPE_LONG){
            dest.writeLong((long)obj);
        }else if(type == TYPE_CHAR){
            dest.writeSerializable((Serializable) obj);
        }else if(type == TYPE_FLOAT){
            dest.writeFloat((float)obj);
        }else if(type == TYPE_DOUBLE){
            dest.writeDouble((double)obj);
        }else if(type == TYPE_BOOL){
            boolean b = (boolean)obj;
            dest.writeInt(b?1:0);
        }else if(type == TYPE_Serializable){
            dest.writeSerializable((Serializable) obj);
        }else if(type == TYPE_Serializable_ARR){
            Log.e("RouteTest","write serializable begin!!!");
            if(obj==null){
                dest.writeInt(0);
            }else{
                dest.writeInt(1);
                int len = Array.getLength(obj);
                dest.writeInt(len);
                dest.writeString(this.objType.getName());
                for (int i=0;i<len;i++){
                    Object item = Array.get(obj,i);
                    if(item == null){
                        dest.writeInt(0);
                    }else {
                        dest.writeInt(1);
                        dest.writeString(item.getClass().getName());
                        dest.writeSerializable((Serializable) item);
                    }
                }
            }
            Log.e("RouteTest","write serializable end!!!");
        }else if(type == TYPE_Parcelable){
            dest.writeParcelable((Parcelable) obj,0);
        }else if(type == TYPE_Parcelable_ARR){
            dest.writeParcelableArray((Parcelable[]) obj,0);
        }
    }
    public static final Creator<KCallArgWrap> CREATOR = new Creator<KCallArgWrap>() {
        @Override
        public KCallArgWrap createFromParcel(Parcel in) {
            return new KCallArgWrap(in);
        }

        @Override
        public KCallArgWrap[] newArray(int size) {
            return new KCallArgWrap[size];
        }
    };
}
