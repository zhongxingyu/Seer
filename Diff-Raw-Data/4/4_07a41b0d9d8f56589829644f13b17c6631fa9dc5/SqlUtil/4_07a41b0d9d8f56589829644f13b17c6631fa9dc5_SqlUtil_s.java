 /**
  * 
  */
 package com.grimesco.gcocentral;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.Arrays;
 
 
 /**
  * @author jaeboston
  *
  */
 public class SqlUtil {
 
 	//-- public function
     
 	//-- https://gcosite.atlassian.net/browse/BREADY-98
 	public static String cleanupDouble(double _number) {
 		
 		BigDecimal b = new BigDecimal(String.format("%.4f", _number), MathContext.DECIMAL64);
 		b = b.stripTrailingZeros();
		return b.toString();
	
 	}
 	
 
 	
 	public static String returnString(char[] charArray) {
 		
 		if (charArray==null) {
 			return null;
 		} else if (Arrays.equals(charArray, new char[ charArray.length])) {  //-- if the initial value
 			return null;
 		}	else {
 			return String.valueOf(charArray);
 		}
 	}
 	
 	public static double returnDouble(char[] charArray) {
 		
 		if (charArray==null) {
 			return (double) 0.0;
 		} else if (Arrays.equals(charArray, new char[ charArray.length])) {  //-- if the initial value
 			return (double) 0.0;
 		}	else {
 			return Double.parseDouble(returnString(charArray));
 		}
 	}
 	
 	
 	public static String returnStringChar(char charV) {
 		
 		if (charV=='\0') {
 			return "N";
 		} else if(charV==' ') {
 			return "N";
 		} else {
 			return String.valueOf(charV);
 		}
 	}
 		
 	
 public static double getDoubleAfterChecktheField(char[] charArray) {
 		
 		//System.out.println("DEBUG: SWsqlUtil.getDoubleAfterChecktheField() : value = '" + String.valueOf(charArray) + "'");
     
 		double defaultValue = 0.0;
 		
 		char[] initvalues = new char[charArray.length];
 	    java.util.Arrays.fill(initvalues, '\u0000');          // Fill array with init
 	    
 	    char[] spaces = new char[charArray.length];
 	    java.util.Arrays.fill(initvalues, ' ');          // Fill array with init
 	    
 	    
 	    //-- check to see if incoming value has na
         if (String.valueOf(charArray).trim().equals("na")) {
         	return defaultValue;
         } else if (Arrays.equals(charArray, initvalues)) {
         	//System.out.println("DEBUG: SWsqlUtil.getDoubleAfterChecktheField() : init error = '" + String.valueOf(charArray) + "'");
         	return defaultValue;
         } else if (Arrays.equals(charArray, spaces)) {
         	//System.out.println("DEBUG: SWsqlUtil.getDoubleAfterChecktheField() : space error = '" + String.valueOf(charArray) + "'");
         	return defaultValue;
         } else {
             return SqlUtil.returnDouble(charArray);
         }
     
 	}//-- end of method
 
 	
 
 	public static String getStringAfterChecktheField(char[] charArray) {
 		
 		String defaultValue = "na";
 		
 		char[] initvalues = new char[charArray.length];
         java.util.Arrays.fill(initvalues, '\u0000');          // Fill array with init
         
         char[] spaces = new char[charArray.length];
 	    java.util.Arrays.fill(initvalues, ' ');          // Fill array with init
 	    
 	    //-- check to see if incoming value has na
         if (String.valueOf(charArray).trim().equals("na")) {
     		//System.out.println("DEBUG getStringAfterChecktheField() case1 : na " );
         	return defaultValue;
         //-- check to see if incoming value has init values
         } else if (Arrays.equals(charArray, initvalues))	{
         	//System.out.println("DEBUG getStringAfterChecktheField() case2 : init" );
         	return defaultValue;
             //-- check to see if incoming value has empty space (init values)
         } else if (Arrays.equals(charArray, spaces))	{
         	//System.out.println("DEBUG getStringAfterChecktheField() case3 : space" );
         	return defaultValue;
         } else {
     		//System.out.println("DEBUG getStringAfterChecktheField() case4 " );
             return String.valueOf(charArray);
         }
     
 	}
 
 	
 	
 }
