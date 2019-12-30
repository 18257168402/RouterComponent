package com.zb.component;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.SparseArray;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.enums.RouteType;
import com.alibaba.android.arouter.facade.model.RouteMeta;
import com.alibaba.android.arouter.facade.template.IProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class KPostcard {
     Postcard mPostcard;
    public KPostcard(Postcard postcard){
        mPostcard = postcard;
    }
    public Bundle getOptionsBundle() {
        return mPostcard.getOptionsBundle();
    }
    public int getEnterAnim() {
        return mPostcard.getEnterAnim();
    }
    public int getExitAnim() {
        return mPostcard.getExitAnim();
    }
    public IProvider getProvider() {
        return mPostcard.getProvider();
    }
    public KPostcard setProvider(IProvider provider) {
        mPostcard.setProvider(provider);
        return this;
    }
    public boolean isGreenChannel() {
        return mPostcard.isGreenChannel();
    }

    public Object getTag() {
        return mPostcard.getTag();
    }

    public KPostcard setTag(Object tag) {
        mPostcard.setTag(tag);
        return this;
    }

    public Bundle getExtras() {
        return mPostcard.getExtras();
    }

    public int getTimeout() {
        return mPostcard.getTimeout();
    }
    /**
     * Set timeout of navigation this time.
     *
     * @param timeout timeout
     * @return this
     */
    public KPostcard setTimeout(int timeout) {
        mPostcard.setTimeout(timeout);
        return this;
    }

    public Uri getUri() {
        return mPostcard.getUri();
    }

    public KPostcard setUri(Uri uri) {
        mPostcard.setUri(uri);
        return this;
    }

    /**
     * Navigation to the route with path in postcard.
     * No param, will be use application context.
     */
    public Object navigation() {
        return navigation(null);
    }

    /**
     * Navigation to the route with path in postcard.
     *
     * @param context Activity and so on.
     */
    public Object navigation(Context context) {
        return navigation(context, null);
    }

//    /**
//     * Navigation to the route with path in postcard.
//     *
//     * @param context Activity and so on.
//     */
//    public Object navigation(Context context, NavigationCallback callback) {
//        return KRouter.navigation(context, this, -1, callback);
//    }

    public Object navigation(Context context,KNavigationCallback callback){
        return KRouter.navigation(context,this,-1,callback);
    }
    public Object navigationForResult(Context context, KNavigationCallback callback){
        return KRouter.navigation(context,this,-1,true,callback);
    }
    /**
     * Navigation to the route with path in postcard.
     *
     * @param mContext    Activity and so on.
     * @param requestCode startActivityForResult's param
     */
    public Object navigation(Context mContext, int requestCode) {
        return KRouter.navigation(mContext,this,requestCode,null);
    }

//    /**
//     * Navigation to the route with path in postcard.
//     *
//     * @param mContext    Activity and so on.
//     * @param requestCode startActivityForResult's param
//     */
//    public void navigation(Activity mContext, int requestCode, NavigationCallback callback) {
//        KRouter.navigation(mContext, this, requestCode, callback);
//    }



    /**
     * Green channel, it will skip all of interceptors.
     *
     * @return this
     */
    public KPostcard greenChannel() {
        mPostcard.greenChannel();
        return this;
    }

    /**
     * BE ATTENTION TO THIS METHOD WAS <P>SET, NOT ADD!</P>
     */
    public KPostcard with(Bundle bundle) {
        mPostcard.with(bundle);
        return this;
    }
    public KPostcard withFlags(@Postcard.FlagInt int flag) {
        mPostcard.withFlags(flag);
        return this;
    }

    public int getFlags() {
        return mPostcard.getFlags();
    }

    /**
     * Set object value, the value will be convert to string by 'Fastjson'
     *
     * @param key   a String, or null
     * @param value a Object, or null
     * @return current
     */
    public KPostcard withObject(@Nullable String key, @Nullable Object value) {
        mPostcard.withObject(key,value);
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
    public KPostcard withString(@Nullable String key, @Nullable String value) {
        mPostcard.withString(key, value);
        return this;
    }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a boolean
     * @return current
     */
    public KPostcard withBoolean(@Nullable String key, boolean value) {
        mPostcard.withBoolean(key, value);
        return this;
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a short
     * @return current
     */
    public KPostcard withShort(@Nullable String key, short value) {
        mPostcard.withShort(key, value);
        return this;
    }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value an int
     * @return current
     */
    public KPostcard withInt(@Nullable String key, int value) {
        mPostcard.withInt(key, value);
        return this;
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a long
     * @return current
     */
    public KPostcard withLong(@Nullable String key, long value) {
        mPostcard.withLong(key, value);
        return this;
    }

    /**
     * Inserts a double value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a double
     * @return current
     */
    public KPostcard withDouble(@Nullable String key, double value) {
        mPostcard.withDouble(key, value);
        return this;
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a byte
     * @return current
     */
    public KPostcard withByte(@Nullable String key, byte value) {
        mPostcard.withByte(key, value);
        return this;
    }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a char
     * @return current
     */
    public KPostcard withChar(@Nullable String key, char value) {
        mPostcard.withChar(key, value);
        return this;
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a float
     * @return current
     */
    public KPostcard withFloat(@Nullable String key, float value) {
        mPostcard.withFloat(key, value);
        return this;
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence, or null
     * @return current
     */
    public KPostcard withCharSequence(@Nullable String key, @Nullable CharSequence value) {
        mPostcard.withCharSequence(key, value);
        return this;
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Parcelable object, or null
     * @return current
     */
    public KPostcard withParcelable(@Nullable String key, @Nullable Parcelable value) {
        mPostcard.withParcelable(key, value);
        return this;
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
    public KPostcard withParcelableArray(@Nullable String key, @Nullable Parcelable[] value) {
        mPostcard.withParcelableArray(key, value);
        return this;
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
    public KPostcard withParcelableArrayList(@Nullable String key, @Nullable ArrayList<? extends Parcelable> value) {
        mPostcard.withParcelableArrayList(key, value);
        return this;
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
    public KPostcard withSparseParcelableArray(@Nullable String key, @Nullable SparseArray<? extends Parcelable> value) {
        mPostcard.withSparseParcelableArray(key, value);
        return this;
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public KPostcard withIntegerArrayList(@Nullable String key, @Nullable ArrayList<Integer> value) {
        mPostcard.withIntegerArrayList(key, value);
        return this;
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public KPostcard withStringArrayList(@Nullable String key, @Nullable ArrayList<String> value) {
        mPostcard.withStringArrayList(key, value);
        return this;
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public KPostcard withCharSequenceArrayList(@Nullable String key, @Nullable ArrayList<CharSequence> value) {
        mPostcard.withCharSequenceArrayList(key, value);
        return this;
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Serializable object, or null
     * @return current
     */
    public KPostcard withSerializable(@Nullable String key, @Nullable Serializable value) {
        mPostcard.withSerializable(key, value);
        return this;
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a byte array object, or null
     * @return current
     */
    public KPostcard withByteArray(@Nullable String key, @Nullable byte[] value) {
        mPostcard.withByteArray(key, value);
        return this;
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a short array object, or null
     * @return current
     */
    public KPostcard withShortArray(@Nullable String key, @Nullable short[] value) {
        mPostcard.withShortArray(key, value);
        return this;
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a char array object, or null
     * @return current
     */
    public KPostcard withCharArray(@Nullable String key, @Nullable char[] value) {
        mPostcard.withCharArray(key, value);
        return this;
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a float array object, or null
     * @return current
     */
    public KPostcard withFloatArray(@Nullable String key, @Nullable float[] value) {
        mPostcard.withFloatArray(key, value);
        return this;
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence array object, or null
     * @return current
     */
    public KPostcard withCharSequenceArray(@Nullable String key, @Nullable CharSequence[] value) {
        mPostcard.withCharSequenceArray(key, value);
        return this;
    }

    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Bundle object, or null
     * @return current
     */
    public KPostcard withBundle(@Nullable String key, @Nullable Bundle value) {
        mPostcard.withBundle(key, value);
        return this;
    }

    /**
     * Set normal transition anim
     *
     * @param enterAnim enter
     * @param exitAnim  exit
     * @return current
     */
    public KPostcard withTransition(int enterAnim, int exitAnim) {
        mPostcard.withTransition(enterAnim,exitAnim);
        return this;
    }

    /**
     * Set options compat
     *
     * @param compat compat
     * @return this
     */
    @RequiresApi(16)
    public KPostcard withOptionsCompat(ActivityOptionsCompat compat) {
        mPostcard.withOptionsCompat(compat);
        return this;
    }

    public Map<String, Integer> getParamsType() {
        return mPostcard.getParamsType();
    }

    public RouteMeta setParamsType(Map<String, Integer> paramsType) {
        return mPostcard.setParamsType(paramsType);
    }



    public RouteType getType() {
        return mPostcard.getType();
    }

    public RouteMeta setType(RouteType type) {
        mPostcard.setType(type);
        return mPostcard;
    }

    public Class<?> getDestination() {
        return mPostcard.getDestination();
    }

    public RouteMeta setDestination(Class<?> destination) {
        mPostcard.setDestination(destination);
        return mPostcard;
    }

    public String getPath() {
        return mPostcard.getPath();
    }

    public RouteMeta setPath(String path) {
        mPostcard.setPath(path);
        return mPostcard;
    }

    public String getGroup() {
        return mPostcard.getGroup();
    }

    public RouteMeta setGroup(String group) {
        mPostcard.setGroup(group);
        return mPostcard;
    }

    public int getPriority() {
        return mPostcard.getPriority();
    }

    public RouteMeta setPriority(int priority) {
        mPostcard.setPriority(priority);
        return mPostcard;
    }

    public int getExtra() {
        return mPostcard.getExtra();
    }

    public RouteMeta setExtra(int extra) {
        mPostcard.setExtra(extra);
        return mPostcard;
    }


}
