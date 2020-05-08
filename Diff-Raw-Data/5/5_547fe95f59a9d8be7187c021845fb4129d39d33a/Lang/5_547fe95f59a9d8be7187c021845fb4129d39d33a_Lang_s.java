 package jzi.view;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 public class Lang {
 	private static Properties lang = new Properties();
 
 	public static void load(String name) {
 		try {
			lang.load(new FileInputStream("lang/" + name + ".properties"));
 		} catch (IOException e) {

 		}
 	}
 
 	public static String get(String key) {
 		return lang.getProperty(key);
 	}
 }
