 package com.tools.tvguide.utils;
 
 import android.app.Application;
 
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
 	}
 }
