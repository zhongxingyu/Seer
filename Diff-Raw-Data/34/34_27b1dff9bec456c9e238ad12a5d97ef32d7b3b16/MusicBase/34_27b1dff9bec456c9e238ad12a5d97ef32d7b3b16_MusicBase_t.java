 package ebs.music;
 
 import java.text.SimpleDateFormat;
 
 /**
  * Created by Aleksey Dubov
  * Date: 3/7/13
  * Time: 10:54 PM
  * Copyright (c) 2013
  */
 public class MusicBase {
	public static final SimpleDateFormat TRUE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 
 	public static String getSolidSteelParts(String part1, String part2) {
 		return (part1.isEmpty() ? "" : "part " + part1) + (part2.isEmpty() ? "" : " & " + part2);
 	}
 }
