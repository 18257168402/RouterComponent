package com.zb.component;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.alibaba.android.arouter.facade.service.SerializationService;
import com.alibaba.android.arouter.launcher.ARouter;

import java.io.Serializable;
import java.util.ArrayList;

public class KNavResult {

    public Bundle mBundle;

    public KNavResult(){
        mBundle = new Bundle();
    }
    public KNavResult(Bundle bundle){
        mBundle = bundle;
    }
    /**
     * Set object value, the value will be convert to string by 'Fastjson'
     *
     * @param key   a String, or null
     * @param value a Object, or null
     * @return current
     */
    public KNavResult withObject(@Nullable String key, @Nullable Object value) {
        SerializationService serializationService = ARouter.getInstance().navigation(SerializationService.class);
        mBundle.putString(key, serializationService.object2Json(value));
        return this;
    }

    // Follow api copy from #{Bundle}

    /**
     * Inserts a String value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a String, or null
     * @return current
     */
    public KNavResult withString(@Nullable String key, @Nullable String value) {
        mBundle.putString(key, value);
        return this;
    }
    public String getString(String key){
        return mBundle.getString(key);
    }
    public String getString(String key,String def){
        return mBundle.getString(key,def);
    }
    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a boolean
     * @return current
     */
    public KNavResult withBoolean(@Nullable String key, boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }
    public Boolean getBoolean(String key){
        return mBundle.getBoolean(key);
    }
    public Boolean getBoolean(String key,Boolean def){
        return mBundle.getBoolean(key,def);
    }
    /**
     * Inserts a short value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a short
     * @return current
     */
    public KNavResult withShort(@Nullable String key, short value) {
        mBundle.putShort(key, value);
        return this;
    }
    public short getShort(String key){
        return mBundle.getShort(key);
    }
    public short getShort(String key,short def){
        return mBundle.getShort(key,def);
    }
    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value an int
     * @return current
     */
    public KNavResult withInt(@Nullable String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }
    public int getInt(String key){
        return mBundle.getInt(key);
    }
    public int getInt(String key,int def){
        return mBundle.getInt(key,def);
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a long
     * @return current
     */
    public KNavResult withLong(@Nullable String key, long value) {
        mBundle.putLong(key, value);
        return this;
    }
    public long getLong(String key){
        return mBundle.getLong(key);
    }
    public long getLong(String key,long def){
        return mBundle.getLong(key,def);
    }
    /**
     * Inserts a double value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a double
     * @return current
     */
    public KNavResult withDouble(@Nullable String key, double value) {
        mBundle.putDouble(key, value);
        return this;
    }

    public double getDouble(String key){
        return mBundle.getDouble(key);
    }
    public double getDouble(String key,double def){
        return mBundle.getDouble(key,def);
    }
    /**
     * Inserts a byte value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a byte
     * @return current
     */
    public KNavResult withByte(@Nullable String key, byte value) {
        mBundle.putByte(key, value);
        return this;
    }
    public byte getByte(String key){
        return mBundle.getByte(key);
    }
    public byte getByte(String key,byte def){
        return mBundle.getByte(key,def);
    }
    /**
     * Inserts a char value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a char
     * @return current
     */
    public KNavResult withChar(@Nullable String key, char value) {
        mBundle.putChar(key, value);
        return this;
    }
    public char getChar(String key){
        return mBundle.getChar(key);
    }
    public char getChar(String key,char def){
        return mBundle.getChar(key,def);
    }
    /**
     * Inserts a float value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a float
     * @return current
     */
    public KNavResult withFloat(@Nullable String key, float value) {
        mBundle.putFloat(key, value);
        return this;
    }
    public float getFloat(String key){
        return mBundle.getFloat(key);
    }
    public float getFloat(String key,float def){
        return mBundle.getFloat(key,def);
    }
    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence, or null
     * @return current
     */
    public KNavResult withCharSequence(@Nullable String key, @Nullable CharSequence value) {
        mBundle.putCharSequence(key, value);
        return this;
    }
    public CharSequence getCharSequence(String key){
        return mBundle.getCharSequence(key);
    }
    public CharSequence getFloat(String key,CharSequence def){
        return mBundle.getCharSequence(key,def);
    }
    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Parcelable object, or null
     * @return current
     */
    public KNavResult withParcelable(@Nullable String key, @Nullable Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }
    public Parcelable getParcelable(String key){
        return mBundle.getParcelable(key);
    }
    /**
     * Inserts an array of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key   a String, or null
     * @param value an array of Parcelable objects, or null
     * @return current
     */
    public KNavResult withParcelableArray(@Nullable String key, @Nullable Parcelable[] value) {
        mBundle.putParcelableArray(key, value);
        return this;
    }
    public Parcelable[] getParcelableArray(String key){
        return mBundle.getParcelableArray(key);
    }
    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList of Parcelable objects, or null
     * @return current
     */
    public KNavResult withParcelableArrayList(@Nullable String key, @Nullable ArrayList<? extends Parcelable> value) {
        mBundle.putParcelableArrayList(key, value);
        return this;
    }
    public  <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key){
        return mBundle.getParcelableArrayList(key);
    }

    /**
     * Inserts a SparceArray of Parcelable values into the mapping of this
     * Bundle, replacing any existing value for the given key.  Either key
     * or value may be null.
     *
     * @param key   a String, or null
     * @param value a SparseArray of Parcelable objects, or null
     * @return current
     */
    public KNavResult withSparseParcelableArray(@Nullable String key, @Nullable SparseArray<? extends Parcelable> value) {
        mBundle.putSparseParcelableArray(key, value);
        return this;
    }
    public  <T extends Parcelable> SparseArray<T> getSparseParcelableArray(String key){
        return mBundle.getSparseParcelableArray(key);
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public KNavResult withIntegerArrayList(@Nullable String key, @Nullable ArrayList<Integer> value) {
        mBundle.putIntegerArrayList(key, value);
        return this;
    }
    public  ArrayList<Integer> getIntegerArrayList(String key){
        return mBundle.getIntegerArrayList(key);
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public KNavResult withStringArrayList(@Nullable String key, @Nullable ArrayList<String> value) {
        mBundle.putStringArrayList(key, value);
        return this;
    }
    public  ArrayList<String> getStringArrayList(String key){
        return mBundle.getStringArrayList(key);
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public KNavResult withCharSequenceArrayList(@Nullable String key, @Nullable ArrayList<CharSequence> value) {
        mBundle.putCharSequenceArrayList(key, value);
        return this;
    }
    public  ArrayList<CharSequence> getCharSequenceArrayList(String key){
        return mBundle.getCharSequenceArrayList(key);
    }
    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Serializable object, or null
     * @return current
     */
    public KNavResult withSerializable(@Nullable String key, @Nullable Serializable value) {
        mBundle.putSerializable(key, value);
        return this;
    }
    public  Serializable getSerializable(String key){
        return mBundle.getSerializable(key);
    }
    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a byte array object, or null
     * @return current
     */
    public KNavResult withByteArray(@Nullable String key, @Nullable byte[] value) {
        mBundle.putByteArray(key, value);
        return this;
    }
    public   byte[] getByteArray(String key){
        return mBundle.getByteArray(key);
    }
    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a short array object, or null
     * @return current
     */
    public KNavResult withShortArray(@Nullable String key, @Nullable short[] value) {
        mBundle.putShortArray(key, value);
        return this;
    }
    public   short[] getShortArray(String key){
        return mBundle.getShortArray(key);
    }
    /**
     * Inserts a char array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a char array object, or null
     * @return current
     */
    public KNavResult withCharArray(@Nullable String key, @Nullable char[] value) {
        mBundle.putCharArray(key, value);
        return this;
    }
    public   char[] getCharArray(String key){
        return mBundle.getCharArray(key);
    }
    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a float array object, or null
     * @return current
     */
    public KNavResult withFloatArray(@Nullable String key, @Nullable float[] value) {
        mBundle.putFloatArray(key, value);
        return this;
    }
    public   float[] getFloatArray(String key){
        return mBundle.getFloatArray(key);
    }
    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence array object, or null
     * @return current
     */
    public KNavResult withCharSequenceArray(@Nullable String key, @Nullable CharSequence[] value) {
        mBundle.putCharSequenceArray(key, value);
        return this;
    }
    public   CharSequence[] getCharSequenceArray(String key){
        return mBundle.getCharSequenceArray(key);
    }
    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Bundle object, or null
     * @return current
     */
    public KNavResult withBundle(@Nullable String key, @Nullable Bundle value) {
        mBundle.putBundle(key, value);
        return this;
    }
    public   Bundle getBundle(String key){
        return mBundle.getBundle(key);
    }
}
