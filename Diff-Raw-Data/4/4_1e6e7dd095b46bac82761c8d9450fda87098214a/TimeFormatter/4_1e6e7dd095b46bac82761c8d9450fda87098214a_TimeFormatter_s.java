 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.speech;
 
 // Utility for formatting times to strings:
 // 9:45 will be formatted to quarter to ten
 // 6:10 will be formatted to 
 public class TimeFormatter {
 	
 	public TimeFormatter() {}
 
 	// round the minutes to its nearest five minutes
 	// 2 should be rounded to 0, 23 to 25, 22 to 20
 	private static int roundMinute(int min) {
 		
 		//hämta entalssiffran.
 		int m = min % 10;
 		// tiotalssiffran
 		int n = min - m;
 		
 		if(m== 0)  {
 			// doesn't need rounding 
 			return n;
 		}else if(m == 1 || m == 2) {
 			return n; 
 		} else if(m == 3 || m==4) {
 			return n + 5;
 		} else if(m == 5) {
 			// doesn't need rounding
 			return min;
 		} else if(m == 6 || m == 7) {
 			return n + 5;
 		} else if(m == 8 || m == 9) {
 			return n + 10;
 		} else {
 			throw new IllegalArgumentException("this should not happen");
 		}
 	}
 	
 	private static String hourToString(int hour) {
 		if(hour == 0) {
 			return "zero";
 		} else if(hour == 1) {
 			return "one";
 		} else if(hour == 2) {
 			return "two";
 		} else if(hour == 3) {
 			return "three";
 		} else if(hour == 4) {
 			return "four";
 		} else if(hour == 5) {
 			return "five";
 		} else if(hour == 6) {
 			return "six";
 		} else if(hour == 7) {
 			return "seven";
 		} else if(hour == 8) {
 			return "eight";
 		} else if(hour == 9) {
 			return "nine";
 		} else if(hour == 10) {
 			return "ten";
 		} else if(hour == 11) {
 			return "eleven";
 		} else if(hour == 12) {
 			return "twelve";
 		} else if(hour == 13) {
 			return "thirteen";
 		} else if(hour == 14) {
 			return "fourteen";
 		} else if(hour == 15) {
 			return "fifteen";
 		} else if(hour == 16) {
 			return "sixteen";
 		} else if(hour == 17) {
 			return "seventeen";
 		} else if(hour == 18) {
 			return "eighteen";
 		} else if(hour == 19) {
 			return "ninteen";
 		} else if(hour == 20) {
 			return "twenty";
 		} else if(hour == 21) {
 			return "twentyone";
 		} else if(hour == 22) {
 			return "twentytwo";
 		} else if(hour == 23) {
 			return "twentythree";
 		} else if(hour == 24) {
 			return "twentyfour";
 		} else {
 			throw new IllegalArgumentException("this should not happen");
 		}
 	}
 	
 	private static String minuteToStringUtil(int min) {
 		if(min == 5) {
 			return "five";
 		} else if(min == 10) {
 			return "ten";
 		} else if(min == 15) {
 			return "quarter";
 		} else if(min == 20) {
 			return "twenty";
 		} else if(min == 25) {
 			return "twenty-five";
 		} else {
 			throw new IllegalArgumentException("this should not happen");
 		}
 	}
 	
 	private static String minuteToString(int min) {
 		if(min == 30) {
 			return "half past";
 		}
 		
 		// is either the value "past" or "to"
 		String pastOrTo;
 		if(min > 30) {
 			pastOrTo ="to";
 			min = 60 -min;
 		} else {
 			pastOrTo = "past";
 		}
 		
 		return minuteToStringUtil(min) + " " + pastOrTo;
 	}
 	
 	// returns a string formatted the way humans read it.
 	// if the language is English, then for 10:30 it will return "half past ten"
 	// for 6:10 it will return "ten past six"
 	public static String formatTime(int hour, int min) {
 		min = roundMinute(min);
 		
 		String hourStr = hourToString(min > 30 ? hour + 1: hour);
 		String minStr = minuteToString(min);
 		return minStr +" "+ hourStr;
 	}
 }
