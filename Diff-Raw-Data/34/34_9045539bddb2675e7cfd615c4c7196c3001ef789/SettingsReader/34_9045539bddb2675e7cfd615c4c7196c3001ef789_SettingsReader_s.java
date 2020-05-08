 package com.jpii.navalbattle.io;
 
 import java.io.*;
 import java.util.*;
 
 public class SettingsReader {
 	ArrayList<SettingsAttribute> attributes;
 	String path;
 	public SettingsReader(String filePath, ArrayList<SettingsAttribute> attributes) {
 		path = filePath;
 		this.attributes = attributes;
 		if (attributes == null || attributes.size() <= 0) {
 			throw new java.lang.IllegalArgumentException("The settings that you would like to read is not filled.");
 		}
 	}
 	public void read() {
 		try {
 			FileInputStream fstream = new FileInputStream(path);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line = "";
 			while ((line = br.readLine()) != null) {
 				int loc = -1;
 				if (line.indexOf(":") > -1)
 					loc = findLineAttributeLocation(line.substring(0,line.indexOf(":")).replace(" ", ""));
 				
 				if (loc >= 0) {
 					String value = line.substring(line.indexOf(":")+1);
 					int c = 0;
 					for (c = 0; c < value.length(); c++) {
 						if (value.charAt(c) != ' ') {
 							break;
 						}
 					}
 					value = value.substring(c);
 					SettingsAttribute attr = attributes.get(loc);
 					attr.setValue(value);
 				}
 			}
 			in.close();
 		}
 		catch (Exception ex) {
 			System.err.println(ex.getMessage());
 		}
 	}
 	private int findLineAttributeLocation(String str) {
 		for (int c = 0; c < attributes.size(); c++) {
 			SettingsAttribute attr = attributes.get(c);
 			if (str.toLowerCase().equals(attr.getName().toLowerCase()))
 				return c;
 		}
 		return -1;
 	}
 	public ArrayList<SettingsAttribute> getData() {
 		return attributes;
 	}
 }
