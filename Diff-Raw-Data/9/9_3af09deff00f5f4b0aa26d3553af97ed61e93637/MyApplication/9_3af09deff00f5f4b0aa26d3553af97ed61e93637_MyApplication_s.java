 package com.tools.tvguide.utils;
 
 import android.app.Application;
 
 public class MyApplication extends Application
 {
	public static MyApplication smInstance = null;
 	
 	public static MyApplication getInstance()
 	{
		return smInstance;
 	}
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
		smInstance = this;
 	}
 }
