 package com.lynk.swing.util;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 
 public class Utils {
 	private static DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
 	public static String getNowStr() {
		return format.format(Calendar.getInstance());
 	}
 }
