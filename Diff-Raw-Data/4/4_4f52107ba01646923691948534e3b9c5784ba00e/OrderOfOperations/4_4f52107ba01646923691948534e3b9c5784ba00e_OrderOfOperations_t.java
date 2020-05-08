 package com.erikleeness.graph.expression;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.erikleeness.graph.expression.functions.Term;
 
 public class OrderOfOperations
 {
 	private static List<Class<? extends Object>> tier3;
 	private static List<Class<? extends Object>> tier2;
 	private static List<Class<? extends Object>> tier1;
 	
 	static {
 		String packagePathPrefix = "com.erikleeness.expression.functions.";
 		try {
 			tier3 = Arrays.asList(Class.forName(packagePathPrefix + "Exponent"), Class.forName(packagePathPrefix + "SquareRoot"));
 			tier2 = Arrays.asList(Class.forName(packagePathPrefix + "Product"), Class.forName(packagePathPrefix + "Quotient"));
 			tier1 = Arrays.asList(Class.forName(packagePathPrefix + "Sum"), Class.forName(packagePathPrefix + "Difference"));
 		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Some programmer fucked up - check the OrderOfOperations class and see that " +
 					"all the classes referenced are valid.");
 		}
 	}
 	
 	public static int comparePrecedence(Term t1, Term t2)
 	{
 		int t1order = getOrder(t1);
 		int t2order = getOrder(t2);
 		
 		return t1order - t2order;
 	}
 	
 	private static int getOrder(Term term)
 	{
 		int order;
 		if (tier3.contains(term.getClass())) {
 			order = 3;
 		} else if (tier2.contains(term.getClass())) {
 			order = 2;
 		} else if (tier1.contains(term.getClass())) {
 			order = 1;
 		} else {
 			order = 3; // If it is not listed, it is assumed to be a function, which are the highest order.
 		}
 		
 		return order;
 	}
 }
