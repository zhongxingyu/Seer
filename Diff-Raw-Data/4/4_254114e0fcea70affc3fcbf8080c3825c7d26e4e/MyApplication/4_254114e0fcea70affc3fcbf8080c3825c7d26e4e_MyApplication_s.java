 package com.tools.tvguide.utils;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.EnvironmentManager;
 import com.tools.tvguide.managers.UrlManager;
 
 import org.acra.ACRA;
 import org.acra.ErrorReporter;
 import org.acra.ReportField;
 import org.acra.ReportingInteractionMode;
 import org.acra.annotation.ReportsCrashes;
 
 import android.app.Application;
 
 @ReportsCrashes(
     formKey = "",
     formUri = UrlManager.ACRA_PROXY_REAL,
     reportType = org.acra.sender.HttpSender.Type.JSON,
     httpMethod = org.acra.sender.HttpSender.Method.PUT,
     formUriBasicAuthLogin = "reporter",
     formUriBasicAuthPassword = "reporter",
     mode = ReportingInteractionMode.TOAST,
 	customReportContent = { ReportField.APP_VERSION_CODE
           , ReportField.APP_VERSION_NAME
           , ReportField.ANDROID_VERSION
           , ReportField.PACKAGE_NAME
           , ReportField.REPORT_ID
 //          , ReportField.BUILD
           , ReportField.PHONE_MODEL
           , ReportField.STACK_TRACE
           , ReportField.CUSTOM_DATA
 //	      , ReportField.LOGCAT
 		  },
 	forceCloseDialogAfterToast = false, // optional, default false
 	resToastText = R.string.crash_toast_text)
 
 public class MyApplication extends Application
 {
 	public static MyApplication sInstance = null;
 	
 	public static MyApplication getInstance()
 	{
 	    assert (sInstance != null);
 		return sInstance;
 	}
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		sInstance = this;
 		
 		ACRA.init(this);
 		AppEngine.getInstance().setApplicationContext(getApplicationContext());
 
 		String url;
 		if (EnvironmentManager.isDevelopMode)
 		{
 		    url = UrlManager.ACRA_PROXY_DEV;
 		}
 		else
 		{
 		    url = UrlManager.ACRA_PROXY_REAL;
 		}
 		ACRA.getConfig().setFormUri(url);
 		
 		// 自定义上报数据
 		// GUID
 		if (AppEngine.getInstance().getUpdateManager().getGUID() != null)
 		    ErrorReporter.getInstance().putCustomData("GUID", AppEngine.getInstance().getUpdateManager().getGUID());
 		
 		// 网络状态
 		if (Utility.isWifi(MyApplication.getInstance()))
 		    ErrorReporter.getInstance().putCustomData("NETWORK", "WIFI");
 		else
 		    ErrorReporter.getInstance().putCustomData("NETWORK", "non-WIFI");
 		
 		// 渠道
		ErrorReporter.getInstance().putCustomData("APP_CHANNEL", AppEngine.getInstance().getUpdateManager().getAppChannelName());
		
 	}
 }
