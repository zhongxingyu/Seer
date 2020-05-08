 package org.minsler.day3;
 
 public class Hole {
 
 	public static void main(String [] args){
 		
 		double r = 10;
 		double a = 19;
 		double b = 5;
 		
 		double gip;
 		
 		gip = Math.sqrt(a*a + b*b);
 		
		System.out.println("Гипотенуза равна "+ gip);
 		
 		if (r*2 > gip) {
 			System.out.println("прямоугольник пройдет");
 		} else {
 			System.out.println("прямоугольник не пройдет");
 		}
 		
 	}
 }
