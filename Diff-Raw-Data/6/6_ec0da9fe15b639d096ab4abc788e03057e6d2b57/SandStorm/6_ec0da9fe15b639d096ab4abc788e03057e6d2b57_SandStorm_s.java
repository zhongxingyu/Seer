 package com.niccholaspage.SandStorm;
 
 import com.niccholaspage.SandStorm.language.Parser;
 
 public class SandStorm {
 	private final static String version = "0.1";
 	
 	public static String getVersion(){
 		return version;
 	}
 	
 	public static void main(String[] args){
		System.out.println("Welcome to SandStorm " + version + "! I shall now execute the test script");
 		
 		String[] lines = new String[]{
				"var gabe = \"GABE NEWELL ROCKS\";",
 		};
 		
 		new Parser(lines);
 	}
 }
