 package edu.cmu.ri.mrpl.util;
 
 import static java.lang.Math.PI;
 
 public class AngleMath {	
 	public static double roundTo2 (double angle) {
 		return ((int) (angle*100)) / 100.0;
 	}
 	
 	public static double calculateTurnAngle (String commands) {
 		// get initial heading correct
 		int i = 0;
 		double theta = 0;
 		while (i > -1) {
 			switch (commands.charAt(i)) {
 			case 'G':
				i = -1;
 				break;
 			case 'L':
 				theta += PI/2;
 				break;
 			case 'R':
 				theta -= PI/2;
 				break;
 			}
 			i++;
 		}
 		return theta;
 	}
 }
