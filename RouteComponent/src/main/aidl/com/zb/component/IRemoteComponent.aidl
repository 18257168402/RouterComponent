// IRemoteComponent.aidl
package com.zb.component;
import com.zb.component.KRemoteMessage;
// Declare any non-default types here with import statements

interface IRemoteComponent {
   void report(in KRemoteMessage msg);
   void onReport(in KRemoteMessage msg);
   void navigationActivity(in KRemoteMessage msg);
   void providerCall(in KRemoteMessage msg,out KRemoteMessage reply);
   void post(in KRemoteMessage msg);
}
