 package com.lynk.swing.util;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
import java.util.Date;
 
 public class Utils {
 	private static DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
 	public static String getNowStr() {
		return format.format(new Date());
 	}
 }
