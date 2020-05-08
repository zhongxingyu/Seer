 package org.csie.mpp.buku;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URLConnection;
 
 public class Util {
 	public static String convertISBN(String isbn13) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(isbn13.substring(3,12));
 		int sum = 0;
 		for(int i = 0; i < 9; ++i) {
 			sum += (isbn13.charAt(i+3) - '0') * (10 - i);
 		}
 		int m = sum % 11;
 		if(m == 1)
 			builder.append('X');
 		else if(m == 0)
 			builder.append(0);
 		else
			builder.append(11-m);
 		return builder.toString();
 	}
 	
 	public static String connectionToString(URLConnection conn) {
 		StringBuilder builder = new StringBuilder();
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return builder.toString();
 	}
 }
