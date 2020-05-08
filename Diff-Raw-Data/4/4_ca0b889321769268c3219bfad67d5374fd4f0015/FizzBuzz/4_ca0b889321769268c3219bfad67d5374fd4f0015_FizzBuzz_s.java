 package com.github.kpacha.jkata.fizzBuzz;
 

 public class FizzBuzz {
 
     public static Object generate(int total) {
 	Object answer = null;
 	if (total == 3) {
 	    answer = "Fizz";
 	} else {
 	    answer = total;
 	}
 	return answer;
     }
 }
