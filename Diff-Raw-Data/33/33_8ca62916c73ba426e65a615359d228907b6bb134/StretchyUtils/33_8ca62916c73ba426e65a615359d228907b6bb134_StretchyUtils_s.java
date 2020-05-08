 package com.rightmove.es.utils;
 
 import org.elasticsearch.common.geo.GeoPoint;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Random;
 
 public class StretchyUtils {
 
 	public static GeoPoint getRandomLocation() {
 		long minSize = 0;
 		long maxSize = 150;
 		Random random = new Random();
 		double latitude = (minSize + (maxSize - minSize)) * random.nextDouble();
 		double longitude = (minSize + (maxSize - minSize)) * random.nextDouble();
 		return new GeoPoint(latitude, longitude);
 	}
 
 	public static Double generateRandomBoost() {
		if(new Random().nextDouble() <= 0.01) {
			return 1.05;
 		}
 		return 1.0;
 	}
 
 	public static File[] getAllJSONFiles() {
 		return new File("I:\\properties").listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith("properties") && pathname.getName().endsWith(".json");
 			}});
 	}
 }
