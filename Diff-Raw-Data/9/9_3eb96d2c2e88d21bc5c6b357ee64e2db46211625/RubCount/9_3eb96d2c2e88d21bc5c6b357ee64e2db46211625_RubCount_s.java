 package org.minsler.day3;
 
 public class RubCount {
 
 	public static void main(String[] args) {
 
 		String rubMany = "рублей";
 		String rubTwo = "рубля";
		String rubOne = "рубльо";
 
 		for (int rub = 99; rub < 124; rub++) {
 
 			int mod = rub % 10;
 			int modHundread = rub % 100;
 
 			if (modHundread > 4 && modHundread < 21) {
 				System.out.println(rub + " ..." + rubMany);
 			} else if(mod == 0 || mod > 4 && mod < 10) {
 				System.out.println(rub + " ..." + rubMany);
 			} else if (mod == 1) {
 				System.out.println(rub + " ..." + rubOne);
 			} else if (mod > 1 && mod < 5) {
 				System.out.println(rub + " ..." + rubTwo);
 			} 
 
 		}
 
 	}
 }
