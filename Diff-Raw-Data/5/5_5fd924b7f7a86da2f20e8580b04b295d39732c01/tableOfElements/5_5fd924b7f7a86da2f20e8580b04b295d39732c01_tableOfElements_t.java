 package com.example.forestgame.element;
 
public class TableOfElements {
 
 	//Constants - names of elements
 	public final static int ELEMENT_NUT = 0;
 	public final static int ELEMENT_MEGA_NUT = 1;
 	
 	//Table includes info about elements:
 	//column 0 - name of current element
 	//column 1 - name of next level element
	//column 2 - scores
 	private static final int[][] TABLE_OF_ELEMENTS = {
 		{ELEMENT_NUT, ELEMENT_MEGA_NUT, 100}
 	};
 	
 	
 	public static int getNextLvl(int name) {
 		return TABLE_OF_ELEMENTS[name][1];
 	}
 	
 	public static int getPoints(int name) {
 		return TABLE_OF_ELEMENTS[name][2];
 	}
 	
 }
