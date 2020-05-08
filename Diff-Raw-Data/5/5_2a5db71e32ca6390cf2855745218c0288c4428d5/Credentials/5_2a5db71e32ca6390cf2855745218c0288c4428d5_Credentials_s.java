 package com.nguyenmp.gauchospace;
 
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 
 public class Credentials {
 	private static String mUsername = null;
 	private static String mPassword = null;
 
 	private static void init() throws IOException {
 		// the username and password should be stored as the first
 		// and second line in credentials
 		FileReader fReader = new FileReader("credentials");
 		BufferedReader reader = new BufferedReader(fReader);
 		mUsername = reader.readLine();
 		mPassword = reader.readLine();
 	}
 
 	public static String Username() throws IOException {
 		// If we already loaded a username before, return that!
 		// Otherwise, read a new one
		if (mUsername != null) init();
 
 		return mUsername;
 	}
 
 	public static String Password() throws IOException {
 		// If we already loaded a password before, return that!
 		// Otherwise, read a new one
		if (mPassword != null) init();
 
 		return mPassword;
 	}
 }
