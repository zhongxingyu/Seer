  package commonenvironment;
 
 import java.util.Comparator;
 
 /**
  * 
  * Sorts strings in alphanumeric order. "A2" will come before "A10". Note: This sorter
  * will fail to sort negative numbers in order. Depending on the numbersBeforeLetters setting,
  * this comparator should sort the strings "-2", "-1", "0", "1", "2" as:
  * <ul>
  * <li>"0", "1", "2", "-1", "-2"</li>
  * <li>"-1", "-2", "0", "1", "2"</li> 
  * </ul>
  * @author Nathaniel Sherry, 2009
  *
  */
 
 public class AlphaNumericComparitor implements Comparator<String> {
 
 	private int numberAndLetter;
 	private int letterAndNumber;
 	
 	public AlphaNumericComparitor(){
 		init(true);
 	}
 	
 	public AlphaNumericComparitor(boolean numbersBeforeLetters){	
 		init(numbersBeforeLetters);
 	}
 	
 	private void init(boolean numbersBeforeLetters){
 		if (numbersBeforeLetters){
 			numberAndLetter = -1;
 			letterAndNumber = 1;
 		} else {
 			numberAndLetter = 1;
 			letterAndNumber = -1;
 		}
 	}
 	
 	
 	
 	public int compare(String string0, String string1) {	
 		
		string0 = string0.toLowerCase();
		string1 = string1.toLowerCase();
 		
 		int length = string0.length();
 		if (string1.length() < length) length = string1.length();
 		
 		String buffer0 = "", buffer1 = "";
 		char char0, char1;
 		boolean char0IsDigit, char1IsDigit;
 		boolean numericMode = false;
 		long number0, number1;
 		
 		//go over these two strings one character at a time
 		for (int i = 0; i < length; i++){
 
 			//get the characters at this location
 			char0 = string0.charAt(i);
 			char1 = string1.charAt(i);
 			
 			char0IsDigit = Character.isDigit(char0);
 			char1IsDigit = Character.isDigit(char1);
 			
 			
 			if (char0IsDigit && !char1IsDigit){
 				//string 0 has a digit here, but string 1 does not
 				
 				if (numericMode){
 					//string1 stopped being a number before string 0 did
 					//this means that string 0's number is bigger, so string 0 is 'greater than'
 					return 1;
 				} else {
 					//what we return here depends on if letters should appear before/higher/sooner numbers or vice versa
 					return numberAndLetter;
 				}
 				
 				
 			} else if (!char0IsDigit && char1IsDigit){
 				//string 1 has a digit here, but string 0 does not
 				
 				if (numericMode){
 					//string0 stopped being a number before string 1 did
 					//this means that string 1's number is bigger, so string 0 is 'less than'
 					return -1;
 				} else {
 					//what we return here depends on if letters should appear before/higher/sooner than numbers or vice versa
 					return letterAndNumber;
 				}
 				
 			} else if (char0IsDigit && char1IsDigit){
 				//both are digits - enter (or stay in) numeric mode to get the whole numbers from each string
 				
 				if (numericMode){
 					buffer0 += char0;
 					buffer1 += char1;
 				} else {
 					buffer0 = "" + char0;
 					buffer1 = "" + char1;
 				}
 				
 				numericMode = true;
 				
 			} else {
 				//they are both not digits
 				
 				if (numericMode){
 					
 					//we were in numeric mode, and both numbers are the same number of digits long
 					number0 = Long.parseLong(buffer0);
 					number1 = Long.parseLong(buffer1);
 					
 					if (number0 == number1){
 						//these numbers are the same
 					} else {
 						//these numbers are not the same, so we return the difference.
 						//given 3 and 7, 3 is less than 7, so we return 3-7=-4 which is <0 so is 'less than'
 						return (int)(number0-number1);
 					}
 					
 					numericMode = false;
 					
 				} else {
 					
 					if (char0 == char1){
 						//they are the same character
 					} else {
						return char0 - char1;
 					}
 					
 				}
 				
 			} //if chars are digits?
 			
 		} //for each character in strings
 		
 		//looks like they're the same after all
 		return 0;
 		
 	}
 
 }
