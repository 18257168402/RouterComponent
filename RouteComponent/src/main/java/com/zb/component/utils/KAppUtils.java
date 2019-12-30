package com.zb.component.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class KAppUtils {

	public static void gotoCallActivity(Context context,String phone){
		Intent intent = new Intent(Intent.ACTION_DIAL);
		Uri data = Uri.parse("tel:" + phone);
		intent.setData(data);
		context.startActivity(intent);
	}

	public static String getCpuArch(){
		return Build.CPU_ABI;
	}

	public static String getPhoneReleaseVersion(){
		return Build.VERSION.RELEASE;
	}
	public static String getPhoneModel(){
		return Build.MODEL;
	}
	public static String getPhoneIMEI(Context context){
		//TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		return getUniquePsuedoID();
	}
	//获得独一无二的Psuedo ID
	public static String getUniquePsuedoID() {
		String serial = null;

		String m_szDevIDShort = "35" +
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

				Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

				Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

				Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

				Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

				Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

				Build.USER.length() % 10; //13 位

		try {
			serial = Build.class.getField("SERIAL").get(null).toString();
			//API>=9 使用serial号
			return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
		} catch (Exception exception) {
			//serial需要一个初始化
			serial = "serial"; // 随便一个初始化
		}
		//使用硬件信息拼凑出来的15位号码
		return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	}

	public static int getAppVersionCode(Context context){
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (Exception e) {
			return 0;
		}
	}

	public static String getApplicationMetaData(Context context,String metakey){
		Context appContext=context.getApplicationContext();
		String metaValue=null;
		try {
			ApplicationInfo appInfo=appContext.getPackageManager()
					.getApplicationInfo(
							appContext.getPackageName(),
							PackageManager.GET_META_DATA);
			if(appInfo.metaData==null){
				return null;
			}
			Object obj=appInfo.metaData.get(metakey);
			if(obj!=null){
				return obj.toString();
			}
		}catch (Exception e){
			e.printStackTrace();
			metaValue=null;
		}
		return metaValue;
	}
	public static String getActivityMetaData(Activity ac, String metakey){
		String metaValue=null;
		try {
			ActivityInfo appInfo=ac.getPackageManager()
					.getActivityInfo(
							ac.getComponentName(),
							PackageManager.GET_META_DATA);
			if(appInfo.metaData==null){
				return null;
			}
			Object obj=appInfo.metaData.get(metakey);
			if(obj!=null){
				return obj.toString();
			}
		}catch (Exception e){
			e.printStackTrace();
			metaValue=null;
		}
		return metaValue;
	}
	public static String getActivityMetaData(Context context,Class<Activity>  acClazz, String metakey){
		Context appContext=context.getApplicationContext();
		String metaValue=null;
		try {
			ComponentName cn=new ComponentName(appContext,acClazz);
			ActivityInfo appInfo=appContext.getPackageManager()
					.getActivityInfo(
							cn,
							PackageManager.GET_META_DATA);
			if(appInfo.metaData==null){
				return null;
			}
			Object obj=appInfo.metaData.get(metakey);
			if(obj!=null){
				return obj.toString();
			}
		}catch (Exception e){
			e.printStackTrace();
			metaValue=null;
		}
		return metaValue;
	}
	public static String getServiceMetaData(Service service,String metakey){
		String metaValue=null;
		try {
			ComponentName cn=new ComponentName(service,service.getClass());
			ServiceInfo appInfo=service.getPackageManager()
					.getServiceInfo(
							cn,
							PackageManager.GET_META_DATA);
			if(appInfo.metaData==null){
				return null;
			}
			Object obj=appInfo.metaData.get(metakey);
			if(obj!=null){
				return obj.toString();
			}
		}catch (Exception e){
			e.printStackTrace();
			metaValue=null;
		}
		return metaValue;
	}
	public static String getServiceMetaData(Context context,Class<Service> serviceClazz,String metakey){
		Context appContext=context.getApplicationContext();
		String metaValue=null;
		try {
			ComponentName cn=new ComponentName(appContext,serviceClazz);
			ServiceInfo appInfo=appContext.getPackageManager()
					.getServiceInfo(
							cn,
							PackageManager.GET_META_DATA);
			if(appInfo.metaData==null){
				return null;
			}
			Object obj=appInfo.metaData.get(metakey);
			if(obj!=null){
				return obj.toString();
			}
		}catch (Exception e){
			e.printStackTrace();
			metaValue=null;
		}
		return metaValue;
	}
	public static String getReceiverMetaData(Context context,
											 Class<BroadcastReceiver> receiverClazz, String metakey){
		Context appContext=context.getApplicationContext();
		String metaValue=null;
		try {
			ComponentName cn=new ComponentName(appContext,receiverClazz);
			ActivityInfo appInfo=appContext.getPackageManager()
					.getReceiverInfo(
							cn,
							PackageManager.GET_META_DATA);
			if(appInfo.metaData==null){
				return null;
			}
			Object obj=appInfo.metaData.get(metakey);
			if(obj!=null){
				return obj.toString();
			}
		}catch (Exception e){
			e.printStackTrace();
			metaValue=null;
		}
		return metaValue;
	}
	public static boolean isAppRuning(Context context,String pkgname){
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> appTasks=am.getRunningTasks(25);
		for (ActivityManager.RunningTaskInfo info:appTasks){
			if(info.topActivity.getPackageName().equals(pkgname)){
				return true;
			}
		}
		return false;
	}

	public static boolean haveActivityInTask(Context context){
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> appTasks=am.getRunningTasks(25);
		for (ActivityManager.RunningTaskInfo info:appTasks){
			if(info.topActivity.getPackageName().equals(context.getPackageName())){
				return true;
			}
		}
		return false;
	}
	public static ActivityManager.RunningTaskInfo getTaskInfo(Context context){
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> appTasks=am.getRunningTasks(25);
		for (ActivityManager.RunningTaskInfo info:appTasks){
			if(info.topActivity.getPackageName().equals(context.getPackageName())){
				return info;
			}
		}
		return null;
	}

	/**
	 * need permisson  <uses-permission android:name="android.permission.GET_TASKS" />
	 */
	public static boolean isTopActivityInTask(Context context,Class activityClazz){
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runtasks = am.getRunningTasks(25);
		if(runtasks!=null){
			for (ActivityManager.RunningTaskInfo info:runtasks){
				ComponentName topActivityComp = info.topActivity;
				if(topActivityComp.getClassName().equals(activityClazz.getName())){
					return true;
				}
			}
		}
		return false;
	}
	public static boolean isBaseActivityInTask(Context context,Class activityClazz){
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runtasks = am.getRunningTasks(15);
		if(runtasks!=null){
			for (ActivityManager.RunningTaskInfo info:runtasks){
				ComponentName baseActivityComp = info.baseActivity;
				if(baseActivityComp.getClassName().equals(activityClazz.getName())){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				//LogUtils.i("screen","appProcess.importance "+appProcess.importance);
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
						appProcess.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
					//LogUtils.i("screen",String.format("Foreground App:", appProcess.processName));
					return false;
				}else{
					//LogUtils.i("screen",String.format("background App:", appProcess.processName));
					return true;
				}
			}
		}
		return true;
	}
	public static boolean isActivityExist(Context context,Intent intent){
		PackageManager pManager=context.getPackageManager();
		ComponentName componentName=intent.resolveActivity(pManager);
		if (componentName !=null) {
			return true;
		}
		return false;
	}
	public static String getAppVersion(Context context){
		String curVersion="";
		try {
			PackageManager pm=context.getPackageManager();
			PackageInfo info=pm.getPackageInfo(context.getPackageName(), 0);
			curVersion=info.versionName;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return curVersion;
	}
	public static String getPackageName(Context context){
		return context.getPackageName();
	}

	public static String getApplicationName(Context context){
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getApplicationContext().getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			applicationInfo = null;
		}
		String applicationName =
				(String) packageManager.getApplicationLabel(applicationInfo);
		return applicationName;
	}
	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
		// Retrieve all services that can match the given intent
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
		// Make sure only one match was found
		if (resolveInfo == null || resolveInfo.size() != 1) {
			return null;
		}
		// Get component info and create ComponentName
		ResolveInfo serviceInfo = resolveInfo.get(0);
		String packageName = serviceInfo.serviceInfo.packageName;
		String className = serviceInfo.serviceInfo.name;
		ComponentName component = new ComponentName(packageName, className);
		// Create a new intent. Use the old one for extras and such reuse
		Intent explicitIntent = new Intent(implicitIntent);
		// Set the component to be explicit
		explicitIntent.setComponent(component);
		return explicitIntent;
	}

	public static String processName(Context context){
		if(context==null){
			return null;
		}
		ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		int myPid = android.os.Process.myPid();
		for (RunningAppProcessInfo info : processInfos) {
			if (info.pid == myPid) {
				return info.processName;
			}
		}
		return null;
	}
	public static boolean isInProcess(Context context,String processname){
		ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		int myPid = android.os.Process.myPid();
		for (RunningAppProcessInfo info : processInfos) {
			if (info.pid == myPid) {
				return info.processName.equals(processname);
			}
		}
		return false;
	}


	public static String getProcessName(Context context){
		ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		String mainProcessName = context.getPackageName();
		int myPid = android.os.Process.myPid();
		for (RunningAppProcessInfo info : processInfos) {
			if (info.pid == myPid) {
				return info.processName;
			}
		}
		return "";
	}
	public static boolean isAppExist(Context context,String pkgName){
		if (TextUtils.isEmpty(pkgName)) {
			return false;
		}
		else {
			try {
				ApplicationInfo info=context.getPackageManager()
						.getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
			} catch (NameNotFoundException  e) {
				// TODO: handle exception
				return false;
			}
		}
		return true;
	}


	public static Drawable getAppIcon(Context context){
		return context.getApplicationInfo().loadIcon(context.getPackageManager());
	}

	public static boolean isActivityResolveable(Context context,Intent intent){
//		List<ResolveInfo> localList=
//				context.getPackageManager().queryIntentActivities(intent,PackageManager.GET_ACTIVITIES);
		return intent.resolveActivity(context.getPackageManager())!=null;
	}

	public static boolean isInstalledApp(Context context,String pkgName){
		final PackageManager packageManager = context.getPackageManager();
		// 获取所有已安装程序的包信息
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		for ( int i = 0; i < pinfo.size(); i++ )
		{
			if(pinfo.get(i).packageName.equalsIgnoreCase(pkgName))
				return true;
		}
		return false;
	}
	public static boolean gotoMarketDetail(Context context,String downloadPkg,String marketPkg){
		try {
			String uristr="market://details?id="+downloadPkg;
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(uristr));
			if(!TextUtils.isEmpty(marketPkg)){
				intent.setPackage(marketPkg);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(intent.resolveActivity(context.getPackageManager())!=null){
				context.startActivity(intent);
				return true;
			}
			return false;
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}
	//获取SIM卡运营商名称 编号和国家
	public static StringBuilder getNetworkOperatorName(Context context){
		StringBuilder stringBuilder=new StringBuilder();
		TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		stringBuilder.append("NetworkOperatorName:").append(telManager.getNetworkOperatorName()+" ")
				.append(telManager.getSubscriberId()+" ")
		.append(telManager.getNetworkCountryIso());
		return stringBuilder;
	}
	public static boolean isMIUI(){
		String device = Build.MANUFACTURER;
		if ( device.equals( "Xiaomi" ) ) {
			try {
				Properties prop = new Properties();
				prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
				return prop.getProperty("ro.miui.ui.version.code", null) != null
						|| prop.getProperty("ro.miui.ui.version.name", null) != null
						|| prop.getProperty("ro.miui.internal.storage", null) != null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static void gotoMiuiPermissionSettings(Context context) {

		// 之兼容miui v8
		try {
			Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
			localIntent.setClassName("com.miui.securitycenter","com.miui.permcenter.permissions.PermissionsEditorActivity");
			localIntent.putExtra("extra_pkgname", context.getPackageName());
			context.startActivity(localIntent);
		} catch (ActivityNotFoundException e) {
			try {
				// MIUI 5/6/7
				Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
				localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
				localIntent.putExtra("extra_pkgname", context.getPackageName());
				context.startActivity(localIntent);
			} catch (Exception e1) {
				// 否则跳转到应用详情
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", context.getPackageName(), null);
				intent.setData(uri);
				context.startActivity(intent);
			}
		}
	}
	public static boolean isAppForeground(Context context, String className) {
		if (context == null || TextUtils.isEmpty(className)) {
			return false;
		}

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
		if (list != null && list.size() > 0) {
			ComponentName cpn = list.get(0).topActivity;
			if (className.equals(cpn.getClassName())) {
				return true;
			}
		}

		return false;
	}
}
