 package com.jeannius.tallycap.validators;
 
 
 import android.util.Log;
 import android.widget.EditText;
 
 public class NumberValidator {
 	
 	public String validate(EditText edittext, Double min, Boolean required, Double max, Boolean isDate){
 				 
 		Log.v("Actual text", edittext.getText().toString());
 		String valid ="";
 		if(isDate){
 			min =1.0;
 			max = 31.0;
 		}
 		//if required and not null		
 		if(required && edittext.getText().length()==0)	valid = "is required";
 		else if(!isGoodNumber(edittext.getText().toString())) valid ="contains invalid characters";
		 else if((required && edittext.getText().length()>0) || (required == false)){			
 			
 			 if(!above_min(min, edittext)){
 				 if(isDate) valid =  String.format("must be between %d and %d", min.intValue(), max.intValue());
 				 else valid="is below minimum value";
 			 }
 			 else  if(!isBelowMax(edittext, max)){
 					 if(isDate) valid =  String.format("must be between %d and %d", min.intValue(), max.intValue());
 					 else valid="is above maximum value";
 			 }
 			 
 			 
 			 else valid="Good";	  
 			 
 			 
 			
 
 			
 			}else valid ="Good";
 		return valid;
 	}
 	
 	public String phoneNumberValidate(EditText editText, Boolean required) {
 		
 		String num = takeDahesOut(editText);
 		String val = "";
 		
 		if(required && editText.getText().length()==0) val ="is required";
 		else if(!isGoodNumber(editText.toString())) val ="contains invalid characters";
 		 else if((required && editText.getText().length()>0) || (required == false)){
 			int firstNum = Integer.parseInt(String.valueOf(num.charAt(0)));
 			
 			if(firstNum>0){
 				if(num.length()<10) val ="must be 10 digits";
 				
 			}
 			else val = "is invalid";
 		 }
 		 
 		 else val = "Good";
 		
 		return val ;
 	}
 
 	
 	
 // this function checks to see if the number entered in the text is above the preset minimum value	 
 	private Boolean above_min(Double min, EditText editText){
 		
 		Boolean is_above_min = true;
 		Double numbertovalidate = Double.valueOf(editText.getText().toString());
 		
 		if(min <= numbertovalidate)	is_above_min = true;
 		
 		else is_above_min = false;
 		
 		
 		return is_above_min;
 	}
 	
 	
 //this function check based on max
 	private Boolean isBelowMax(EditText editText, double max){
 		
 		Boolean BelowMax;
 		double numberToValidate = Double.valueOf(editText.getText().toString());
 		if(max>= numberToValidate) BelowMax=true;
 		else BelowMax=false;
 		return BelowMax;
 	}
 	
 	
 	
 	
 	
 //this functions takes out the dashes from the phone number
 	
 	private String takeDahesOut(EditText red){
 		
 		String s = "";
 		
 		if(red.getText().toString().length()==0) s="";
 		else{
 			String temp = red.getText().toString();
 			String t2 ="";
 			for(int i =0; i<temp.length(); i++){
 				t2 = String.valueOf(temp.charAt(i));
 				if(!t2.equals("-"))
 				s+= t2;
 			}
 			
 		}
 		
 		return s;
 	}
 	
 	
 	
 //	this next function makes sure the text is a valid number
 	private Boolean isGoodNumber(String s){
 		
 		Boolean g= true;
 		boolean decimal =false;
 		Character c;
 	for(int i=0; i<s.length(); i++){
 		
 		c= s.charAt(i);
 		if(!Character.isDigit(c)){
 			
 			Log.v("NUMBER_ERROR", String.format("%s is not a number", c));
 			if(decimal == false){
 				Log.v("NUMBER_ERROR", String.format("%s is a decimal", c));
 				if(c.equals(".")) decimal= true;
 			}
 			else{
 				g=false;
 				break;
 			}
 			
 		}
 	  
 	}
 	
 	
 	return g;
 	}
 	
 	
 	
 	
 	
 	
 	
 
 }
 
 
 
 
