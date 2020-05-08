 package com.example.sprint;
 
 import com.compuware.apm.uem.mobile.android.CompuwareUEM;
 
 import android.app.ActivityManager;
 import android.app.Application;
 import android.content.Context;
 import android.util.Log;
 
 public class SprintApplication extends Application{
 	public static int compuwareCheck;
	public static int enterCheck;
	public static int leaveCheck;
 	
     public void onCreate() {
     	compuwareCheck = CompuwareUEM.startup(this , "Sprint", "http://10.24.16.122:8080/", true, null);
    	
    	enterCheck = CompuwareUEM.enterAction("Sprint Application");
    	leaveCheck = CompuwareUEM.leaveAction("Sprint Application");
     }
     
     public static void memCheck(Context context) {
     	ActivityManager actvityManager = (ActivityManager) context.getSystemService( ACTIVITY_SERVICE );
     	ActivityManager.MemoryInfo mInfo = new ActivityManager.MemoryInfo ();
     	actvityManager.getMemoryInfo( mInfo );
     	// Print to log and read in DDMS
     	Log.i( "TAG", " minfo.availMem " + mInfo.availMem );
     	Log.i( "TAG", " minfo.lowMemory " + mInfo.lowMemory );
     	Log.i( "TAG", " minfo.threshold " + mInfo.threshold );
     }
 
 	@Override
 	public void onTerminate() {
 		CompuwareUEM.shutdown();
 		super.onTerminate();
 	}
 
 }
