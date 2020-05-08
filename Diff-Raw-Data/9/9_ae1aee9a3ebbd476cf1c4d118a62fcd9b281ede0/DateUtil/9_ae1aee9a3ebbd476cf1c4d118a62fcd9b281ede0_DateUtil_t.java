 package com.ghlh.util;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class DateUtil {
 	public static String formatDate(Date date) {
 		Date now = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 		return df.format(date);
 	}
 
 	public static void main(String[] args) {
 		Date now = new Date();
 		System.out.println(formatDate(now));
 		
 	}
 }
