 
 package com.wonderbiz.app.helloeclipse;
 
 import com.wonderbiz.lib.maths.Maths;
 
 public class HelloEclipse {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		System.out.println("Git is fun...!");
 		System.out.println("Eclipse is cool...!!");
 		
 		Maths mymaths = new Maths(7, 2);
 		
 		System.out.println("Maths: ADD = " + mymaths.add());
 		System.out.println("Maths: SUB = " + mymaths.subtract());
 	}
 
 }
 
