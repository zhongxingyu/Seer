 /**
  * 
  */
 package com.mimpidev.podsalinan.cli;
 
 import java.util.Map;
 
 import com.mimpidev.podsalinan.DataStorage;
 
 /**
  * @author sbell
  *
  */
 public abstract class CLIOption {
 
 	/**
 	 * 
 	 */
 	protected DataStorage data;
 	/**
 	 * 
 	 */
 	protected Map<String, CLIOption> options;
 	protected ReturnCall returnObject;
 
 	/**
 	 * 
 	 */
 	public CLIOption(DataStorage newData) {
 		data=newData;
 	}
 
 	public abstract ReturnCall execute(String command);
 	
 	public String getCharForNumber(int i){
 		return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
 	}
 	
 	public String getEncodingFromNumber(int number){
 		String charOutput="";
 		if (number<27)
 			charOutput = getCharForNumber(number);
 		else {
 			if (number%26!=0){
 				charOutput+=getCharForNumber(number/26);
 				charOutput+=getCharForNumber(number%26);
 			} else {
 				charOutput=getCharForNumber((number/26)-1)+"Z";
 			}
 		}
 		
 		return charOutput;
 	}
 	
 	public int convertCharToNumber(String input) {
 		int number=1;
 		if (input.length()>1){
 			for (int charCount=0; charCount < input.length()-1; charCount++)
 				number=number*26*(int)(input.toUpperCase().charAt(charCount)-64);
 			number+=(int)(input.toUpperCase().charAt(input.length()-1)-64);
 		} else if (input.length()==1)
 			number=(int)(input.toUpperCase().charAt(0)-64);
 		number--;
 		
 		return number;
 	}
 }
