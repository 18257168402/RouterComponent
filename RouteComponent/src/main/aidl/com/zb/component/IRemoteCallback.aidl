// IRemoteCallback.aidl
package com.zb.component;
import com.zb.component.KRemoteMessage;
// Declare any non-default types here with import statements

interface IRemoteCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onCallback(in KRemoteMessage msg,out KRemoteMessage reply);
}
