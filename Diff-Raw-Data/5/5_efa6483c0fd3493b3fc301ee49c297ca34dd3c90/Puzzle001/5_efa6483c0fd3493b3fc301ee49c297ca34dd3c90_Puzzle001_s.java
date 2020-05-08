 package net.gageot.puzzler;
 
 /**
  * Oddity.<br/>
  * http://www.dartlang.org/articles/puzzlers/chapter-1.html#1
  */
public class Puzzle001 {
 	public static boolean isOdd(int i) {
 		return i % 2 == 1;
 	}
 
 	public static boolean isOddFixed(int i) {
 		return i % 2 != 0;
 	}
 
 	public static boolean isOddFixedAndFaster(int i) {
 		return (i & 1) != 0;
 	}
 }
