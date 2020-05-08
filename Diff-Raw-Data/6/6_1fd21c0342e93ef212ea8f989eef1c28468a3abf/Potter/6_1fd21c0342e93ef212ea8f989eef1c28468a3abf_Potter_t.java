 package com.github.kpacha.jkata.potter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Potter {
 
     private static Map<Integer, Double> discount = new HashMap<Integer, Double>() {
 	{
 	    put(5, 0.75);
 	    put(4, 0.8);
 	    put(3, 0.9);
 	    put(2, 0.95);
 	    put(1, 1.0);
 	    put(0, 1.0);
 	}
     };
 
     public static double priceFor(int... items) {
	double total = 8 * items[0] * getDiscountFor(items[0]);
 	if (items.length == 2)
	    total += 8 * items[1] * getDiscountFor(items[1]);
	return total;
     }
 
     private static double getDiscountFor(int items) {
 	return discount.get(items);
     }
 }
