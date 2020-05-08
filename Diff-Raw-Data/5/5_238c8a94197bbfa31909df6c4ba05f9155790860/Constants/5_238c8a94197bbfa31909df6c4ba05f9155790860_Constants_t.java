 package com.happyfuncode.giantbomb.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class Constants {
 
 	public static final String API_KEY = Constants.loadAPIKey();
 
 	public static String loadAPIKey() {
 		Properties properties = new Properties();
 		InputStream is = null;
 		try {
			is = Constants.class.getClassLoader().getResourceAsStream(
 					"giantbomb.properties");
 			properties.load(is);
 		} catch (IOException e) {
 			throw new Error(e);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 					throw new Error(e);
 				}
 			}
 		}
 		return properties.getProperty("apiKey");
 	}
 
 }
