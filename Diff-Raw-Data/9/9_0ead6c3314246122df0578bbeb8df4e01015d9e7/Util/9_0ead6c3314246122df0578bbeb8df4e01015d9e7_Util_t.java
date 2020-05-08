 package com.pavlinic.htmlmacros;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 public class Util {
 	public static String resourceAsString(String path) throws IOException {
		return readAll(Util.class.getClassLoader().getResourceAsStream(path));
 	}
 
 	public static String readAll(InputStream stream) throws IOException {
 		Reader reader = new BufferedReader(new InputStreamReader(stream));
 		
 		return readAll(reader);
 	}
 
 	public static String readAll(Reader reader) throws IOException {
 		StringBuilder buf = new StringBuilder();
 		int in;
 		while ((in = reader.read()) != -1) {
 			char ch = (char) in;
 			buf.append(ch);
 		}
 		return buf.toString();
 	}
 }
