 package hello;
 
 public class FizzBuzz {
 
 	public static String fizzbuzz(int i) {
		if (i % 15 == 0) return "FizzBuzz";
		if (i % 3 == 0) return "Fizz";
		if (i % 5 == 0) return "Buzz";
		return String.valueOf(i);
 	}
 
 	public static String fizz(int i) {
 		if (i % 3 == 0) return "Fizz";
 		return "";
 	}
 
 	public static String buzz(int i) {
 		if (i % 5 == 0) return "Buzz";
 		return "";
 	}
 
 }
