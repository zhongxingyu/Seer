 package com.github.kpacha.jkata.fizzBuzz;
 
import java.util.ArrayList;
 import java.util.List;
 
 public class FizzBuzz {
 
     public static List<Object> generate(int total) {
	List<Object> answer = new ArrayList<Object>(total);
	for (int i = 1; i <= total; i++) {
	    answer.add(i);
	}
	return answer;
     }
 }
