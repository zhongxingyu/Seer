 package org.mess110.servusberry.util;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Util {
 
 	public static void log(String text) {
 		Log.d(ServusConst.LOG_TAG, text);
 	}
 
 	public static void log(int i) {
 		log(String.valueOf(i));
 	}
 
 	public static String pathJoin(String s1, String s2) {
 		if (s1.endsWith("/")) {
 			s1 = s1.substring(0, s1.length() - 1);
 		}
 		if (!s2.startsWith("/")) {
 			s1 += "/";
 		}
 		return s1 + s2;
 	}
 
 	public static String prevDir(String path) {
 		String s = path;
 		if (s.indexOf("/") != -1) {
 			s = s.substring(0, s.lastIndexOf("/"));
 		}
 		return s;
 	}
 
 	public static void toast(Context context, String text) {
 		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
 	}
 }
