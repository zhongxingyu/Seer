 package com.github.kpacha.jkata.romanNumerals;
 
 public class RomanNumerals {
 
     public static String convert(int number) {
 	String roman = "";
 	while (number >= 1) {
 	    roman += "I";
 	    number--;
 	}
 	return roman;
     }
 }
