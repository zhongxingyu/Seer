 package com.stormdev.minigamez.editor;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class CustomGameCreator {
 	public static void main(String[] args) {
		InputStream stream = CustomGameCreator.class.getResourceAsStream("plugin.yml"); //Make sure resource is copied to jar on export
		try {
			stream.close();
		} catch (IOException e) {
			//Do nothing
		}
 		System.out.println("Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! ");
         return;
 	}
 
 }
