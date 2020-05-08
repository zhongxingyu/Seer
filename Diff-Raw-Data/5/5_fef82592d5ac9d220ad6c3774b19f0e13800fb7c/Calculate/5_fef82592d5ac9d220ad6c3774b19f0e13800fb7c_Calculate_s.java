 package com.simple.calculator;
 
 
 import java.util.ArrayList;
 import java.math.BigDecimal;
 
 public class Calculate {
 	
 	static String result = "0";
 	
 	public Calculate(ArrayList<String> list){
 		
 		ArrayList<String> array = list;
 		
 		for (int i = 0; i < array.size(); i++){	// Searches for (
 			if (array.get(i).equals("(")){
 				int count = 1;
 				int j = i + 1;
 				while (count != 0){	// Searches for the right )
 					if (array.get(j).equals("(")){
 						count++;
 					}
 					else if (array.get(j).equals(")")){
 						count--;
 					}
 					if (count == 0){
 						break;
 					}
 					j++;
 				}
 				ArrayList<String> subArray = new ArrayList<String>();	// makes a new subarraylist
 				for (int k = i + 1; k < j; k++){	// Adds the objects between the brackets into the subarraylist
 					subArray.add(array.get(k));
 				}
 				new Calculate(subArray);	// Calls Calculate(subarray)(recursion)
 				String q = getResult();
 				array.set(i, q);
 				for(int l = j; l > i; l--){
 					array.remove(l);
 				}
 			}	
 		}
 		
 		for (int i = 0; i < array.size(); i++){	// Searches for ² and √
 			if (array.get(i).equals("²") || array.get(i).equals("√")){
 				if (array.get(i).equals("²")){	// Calculates x² by calling square(x)
 					String x = square(array.get(i- 1));
 					array.set(i, x);
 					result = array.get(i);
 					array.remove(i - 1);
 					i--;
 				}
 				else{	// Calculates √x by calling squareRoot(x)
 					String x = squareRoot(array.get(i + 1));
 					array.set(i, x);
 					result = array.get(i);
 					array.remove(i + 1);
 				}
 			}
 		}
 		
 		for (int i = 0; i < array.size(); i++){	// Searches for * and ÷
 			if (array.get(i).equals("*") || array.get(i).equals("÷")){
 				if (array.get(i).equals("*")){	// Calculates x * y by calling multiplication(x, y)
 					String x = multiplication(array.get(i- 1), array.get(i + 1));
 					array.set(i, x);
 					result = array.get(i);
 					array.remove(i + 1);
 					array.remove(i - 1);
 					i--;
 				}
 				else{	// Calculates x / y by calling division(x, y)
 					String x = division(array.get(i- 1), array.get(i + 1));
 					array.set(i, x);
 					result = array.get(i);
 					array.remove(i + 1);
 					array.remove(i - 1);
 					i--;
 				}
 			}
 		}
 		
 		for (int i = 0; i < array.size(); i++){	// Searches for + and -
 			if (array.get(i).equals("+") || array.get(i).equals("-")){
 				if (array.get(i).equals("+")){	// Calculates x + y by calling addition(x, y)
 					String x = addition(array.get(i- 1), array.get(i + 1));
 					array.set(i, x);
 					result = array.get(i);
 					array.remove(i + 1);
 					array.remove(i - 1);
 					i--;
 				}
 				else{	// Calculates x - y by calling subtraction(x, y)
 					String x = subtraction(array.get(i- 1), array.get(i + 1));
 					array.set(i, x);
 					result = array.get(i);
 					array.remove(i + 1);
 					array.remove(i - 1);
 					i--;
 				}
 			}
 		}
 		if (array.size() == 1){
 			result = array.get(0);
 		}
 	}
 	
 	public static String addition(String x, String y){	// Calculates x + y
 		BigDecimal xx = new BigDecimal(x);
 		BigDecimal yy = new BigDecimal(y);
 		BigDecimal zz = xx.add(yy);
 		return zz.toString();
 	}
 	
 	public static String subtraction(String x, String y){	// Calculates x - y
 		BigDecimal xx = new BigDecimal(x);
 		BigDecimal yy = new BigDecimal(y);
 		BigDecimal zz = xx.subtract(yy);
 		return zz.toString();
 	}
 	
 	public static String multiplication(String x, String y){	// Calculates x * y
 		BigDecimal xx = new BigDecimal(x);
 		BigDecimal yy = new BigDecimal(y);
 		BigDecimal zz = xx.multiply(yy);
 		return zz.toString();
 	}
 	
 	public static String division(String x, String y){	// Calculates x / y
 		BigDecimal xx = new BigDecimal(x);
 		BigDecimal yy = new BigDecimal(y);
		BigDecimal zz = xx.divide(yy);
 		return zz.toString();
 	}
 	
 	public static String square(String x){	// Calculates x²
 		BigDecimal xx = new BigDecimal(x);
 		BigDecimal zz = xx.multiply(xx);
 		return zz.toString();
 	}
 	
 	public static String squareRoot(String x){	// Calculates √x
 		double xx = Double.parseDouble(x);
 		double zz = Math.sqrt(xx);
 		return Double.toString(zz);
 	}
 	
 	public static String getResult(){	// Returns the result
 		return result;
 	}
 	
 	public static ArrayList<String> equation(double a, double b, double c){	// Solves first and second degree equations
 		double result1;
 		ArrayList<String> result3 = new ArrayList<String>();
 		if (a == 0){
 			result1 = (-c) / b;
 			result3.add(Double.toString(result1));
 		}
 		else{
 			result1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
 			double result2 = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
 			result3.add(Double.toString(result1));
 			result3.add(Double.toString(result2));
 		}
 		return result3;
 	}
 }
