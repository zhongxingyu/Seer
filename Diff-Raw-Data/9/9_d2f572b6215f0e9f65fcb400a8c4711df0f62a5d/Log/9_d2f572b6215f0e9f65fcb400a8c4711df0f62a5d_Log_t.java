 package de.da_sense.moses.client.util;
 
 public class Log {
	public static int LEVEL = android.util.Log.VERBOSE;
 
 	static public void d(String tag, String msg) {
 		if (LEVEL <= android.util.Log.DEBUG)
 			android.util.Log.d(tag, msg);
 	}
 
 	static public void i(String tag, String msg) {
 		if (LEVEL <= android.util.Log.INFO)
 			android.util.Log.i(tag, msg);
 	}
 
 	static public void e(String tag, String msg) {
 		if (LEVEL <= android.util.Log.ERROR)
 			android.util.Log.e(tag, msg);
 	}
 
 	static public void v(String tag, String msg) {
 		if (LEVEL <= android.util.Log.VERBOSE)
 			android.util.Log.v(tag, msg);
 	}
 
 	static public void w(String tag, String msg) {
 		if (LEVEL <= android.util.Log.WARN)
 			android.util.Log.w(tag, msg);
 	}
 
 	static public void wtf(String tag, String msg) {
 		if (LEVEL <= android.util.Log.ASSERT)
 			android.util.Log.wtf(tag, msg);
 	}
 
 	static public void d(String tag, String msg, Throwable tr) {
 		if (LEVEL <= android.util.Log.DEBUG)
 			android.util.Log.d(tag, msg, tr);
 	}
 
 	static public void i(String tag, String msg, Throwable tr) {
 		if (LEVEL <= android.util.Log.INFO)
 			android.util.Log.i(tag, msg, tr);
 	}
 
 	static public void e(String tag, String msg, Throwable tr) {
 		if (LEVEL <= android.util.Log.ERROR)
 			android.util.Log.e(tag, msg, tr);
 	}
 
 	static public void v(String tag, String msg, Throwable tr) {
 		if (LEVEL <= android.util.Log.VERBOSE)
 			android.util.Log.v(tag, msg, tr);
 	}
 
 	static public void w(String tag, String msg, Throwable tr) {
 		if (LEVEL <= android.util.Log.WARN)
 			android.util.Log.w(tag, msg, tr);
 	}
 
 	static public void wtf(String tag, String msg, Throwable tr) {
 		if (LEVEL <= android.util.Log.ASSERT)
 			android.util.Log.wtf(tag, msg, tr);
 	}
 }
