 package main;
 
 import GUI.MainGUI;
 
 public class BlasterCardListener {
 	
 	private static final String start = ";984000017";
 	private static final String error = "E?";
 	
 	public static String strip(String input) {
 		if (input.contains(start)) {
 			// if the input starts with ;984000017, strips the next 8 digits, and set it as the input.
 			input = input.split(start)[1].substring(0, 8);
 			return input;
 		} else if (input.contains(error)) {
 			MainGUI.showMessage("Card read error. Please try again.");
 			return "";
 		} else if (input.length() != 8){
			MainGUI.showMessage("Please enter an 8-digit CWID, numbers only.");
 			return "";
 		} else {
 			return input;
 		}
 	}
 	
 }
